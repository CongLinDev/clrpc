package conglin.clrpc.transfer;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.net.IPAddressUtils;
import conglin.clrpc.service.discovery.BasicServiceDiscovery;
import conglin.clrpc.service.discovery.ServiceDiscovery;
import conglin.clrpc.service.loadbalance.ConsistentHashLoadBalancer;
import conglin.clrpc.service.loadbalance.LoadBalancer;
import conglin.clrpc.transfer.handler.BasicConsumerChannelInitializer;
import conglin.clrpc.transfer.handler.ConsumerChannelInitializer;
import conglin.clrpc.transfer.receiver.ResponseReceiver;
import conglin.clrpc.transfer.sender.RequestSender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ConsumerTransfer {

    private static final Logger log = LoggerFactory.getLogger(ConsumerTransfer.class);

    public final String LOCAL_ADDRESS;

    private EventLoopGroup workerGroup;

    private final ReentrantLock lock;
    private final Condition connected;

    private RequestSender sender;
    private ResponseReceiver receiver;

    private ServiceDiscovery serviceDiscovery;
    private LoadBalancer<String, String, Channel> loadBalancer;

    public ConsumerTransfer() {
        loadBalancer = new ConsistentHashLoadBalancer<>(
            (addr, ch)->((InetSocketAddress)ch.remoteAddress()).toString().compareTo(addr) == 0);
        serviceDiscovery = new BasicServiceDiscovery();

        lock = new ReentrantLock();
        connected = lock.newCondition();

        LOCAL_ADDRESS = ConfigParser.getOrDefault("consumer.address", "localhost:5200");
    }

    /**
     * 开启传输服务
     * @param sender
     * @param serviceHandler
     */
    public void start(RequestSender sender, ResponseReceiver receiver) {
        this.sender = sender;
        this.receiver = receiver;

        if (workerGroup == null) {
            int workerThread = ConfigParser.getOrDefault("consumer.thread.worker", 4);
            workerGroup = new NioEventLoopGroup(workerThread);
        }
    }

    /**
     * 在 ZooKeeper中寻找服务提供者
     * @param interfaceClass 提供该服务的类
     */
    public void subscribeService(Class<?> interfaceClass) {
        subscribeService(interfaceClass.getSimpleName());
    }

    /**
     * 在 ZooKeeper中寻找服务提供者
     * @param serviceName
     */
    public void subscribeService(String serviceName){
        serviceDiscovery.registerConsumer(serviceName, LOCAL_ADDRESS);
        serviceDiscovery.discover(serviceName, this::updateConnectedProvider);
    }

    /**
     * 停止服务
     */
    public void stop() {
        disconnectAllProviderNode();
        signalAvailableChannelHandler();

        if (workerGroup != null)
            workerGroup.shutdownGracefully();
        serviceDiscovery.stop();

        sender.stop();
        receiver.stop();
    }


    /**
     * 获取请求发送器
     * @return the sender
     */
    public RequestSender getSender() {
        return sender;
    }

    /**
     * 更新连接的服务器
     * 
     * @param serviceName   服务名
     * @param providerAddress 服务器地址
     */
    public void updateConnectedProvider(String serviceName, List<String> providerAddress) {
        loadBalancer.update(serviceName, providerAddress, 
            addr -> connectProviderNode(serviceName, addr),
            Channel::close);
        signalAvailableChannelHandler();
    }

    /**
     * 取消连接所有节点的服务器
     */
    private void disconnectAllProviderNode() {
        loadBalancer.forEach(Channel::close);
    }

    /**
     * 连接某个特定的服务提供者
     * @param serviceName
     * @param remoteAddress
     */
    private Channel connectProviderNode(String serviceName, String remoteAddress) {    
        Bootstrap bootstrap = new Bootstrap();
        ConsumerChannelInitializer channelInitializer = new BasicConsumerChannelInitializer(receiver);
        bootstrap.localAddress(IPAddressUtils.getPort(LOCAL_ADDRESS))
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(channelInitializer);
        log.info("Consumer started on {}", LOCAL_ADDRESS);

        InetSocketAddress socketRemoteAddress = IPAddressUtils.splitHostnameAndPortSilently(remoteAddress);
        try {
            bootstrap.connect(socketRemoteAddress).sync();
        } catch (InterruptedException e1) {
            log.error("Cannot connect to remote provider. Remote Address : " + remoteAddress);
            return null;
        }
        log.debug("Connect to remote provider successfully. Remote Address : " + remoteAddress);
        return channelInitializer.channel();
    }

    /**
     * 挑选服务发布者
     * @param serviceName 服务名
     * @param random 随机因子
     * @return
     */
    private Channel chooseChannel(String serviceName, int random){
        while(!loadBalancer.hasNext(serviceName)){
            try{
                waitingForChannelHandler();
            }catch(InterruptedException e){
                log.error("Waiting for available node is interrupted!", e);
            }
        }
        return loadBalancer.get(serviceName, random);
    }

    /**
     * 挑选 Channel 进行发送
     * @param serviceName
     * @param object
     * @return
     */
    public Channel chooseChannel(String serviceName, Object object){
        if(!(object instanceof String)) 
            return chooseChannel(serviceName, object.hashCode());
        return loadBalancer.get(serviceName, (String)object);
    }

    /**
     * 等待可用的服务提供者
     * @return
     * @throws InterruptedException
     */
    private boolean waitingForChannelHandler() throws InterruptedException{
        lock.lock();
        long timeout = ConfigParser.getOrDefault("consumer.session.timeout", 5000);
        try{
            log.info("Waiting for Channel Handler " + timeout + " mm...");
            return connected.await(timeout,TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 有可用的ChannelHandler存在
     * 唤醒等待的线程
     */
    private void signalAvailableChannelHandler(){
        lock.lock();
        try{
            connected.signalAll();
        }finally{
            lock.unlock();
        }
    }
}