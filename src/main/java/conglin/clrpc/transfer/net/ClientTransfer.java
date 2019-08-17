package conglin.clrpc.transfer.net;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.net.IPAddressUtils;
import conglin.clrpc.service.discovery.BasicServiceDiscovery;
import conglin.clrpc.service.discovery.ServiceDiscovery;
import conglin.clrpc.service.loadbalance.ConsistentHashHandler;
import conglin.clrpc.service.loadbalance.LoadBalanceHandler;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import conglin.clrpc.transfer.net.handler.BasicClientChannelInitializer;
import conglin.clrpc.transfer.net.handler.ClientChannelInitializer;
import conglin.clrpc.transfer.net.receiver.ResponseReceiver;
import conglin.clrpc.transfer.net.sender.RequestSender;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientTransfer {

    private static final Logger log = LoggerFactory.getLogger(ClientTransfer.class);

    public final String LOCAL_ADDRESS;

    private EventLoopGroup workerGroup;

    private final ReentrantLock lock;
    private final Condition connected;

    private RequestSender sender;
    private ResponseReceiver receiver;

    private ServiceDiscovery serviceDiscovery;
    private LoadBalanceHandler<String, String, BasicClientChannelHandler> loadBalanceHandler;

    public ClientTransfer() {
        loadBalanceHandler = new ConsistentHashHandler<>();
        serviceDiscovery = new BasicServiceDiscovery();

        lock = new ReentrantLock();
        connected = lock.newCondition();

        LOCAL_ADDRESS = ConfigParser.getOrDefault("client.address", "localhost:5200");
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
            int workerThread = ConfigParser.getOrDefault("client.thread.worker", 4);
            workerGroup = new NioEventLoopGroup(workerThread);
        }
    }

    /**
     * 在 ZooKeeper中寻找服务提供者
     * @param interfaceClass 提供该服务的类
     */
    public void findService(Class<?> interfaceClass) {
        findService(interfaceClass.getSimpleName());
    }

    /**
     * 在 ZooKeeper中寻找服务提供者
     * @param serviceName
     */
    public void findService(String serviceName){
        serviceDiscovery.registerConsumer(serviceName, LOCAL_ADDRESS);
        serviceDiscovery.discover(serviceName, this::updateConnectedServer);
    }

    /**
     * 停止服务
     */
    public void stop() {
        disconnectAllServerNode();
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
     * @param serverAddress 服务器地址
     */
    public void updateConnectedServer(String serviceName, List<String> serverAddress) {
        loadBalanceHandler.update(serviceName, serverAddress, 
            addr -> connectServerNode(serviceName, addr),
            channelHandler -> channelHandler.close()
        );
        signalAvailableChannelHandler();
    }

    /**
     * 取消连接所有节点的服务器
     */
    private void disconnectAllServerNode() {
        loadBalanceHandler.forEach(channelHandler -> channelHandler.close());
    }

    /**
     * 连接某个特定的服务提供者
     * @param serviceName
     * @param remoteAddress
     */
    private BasicClientChannelHandler connectServerNode(String serviceName, String remoteAddress) {    
        Bootstrap bootstrap = new Bootstrap();
        ClientChannelInitializer channelInitializer = new BasicClientChannelInitializer(receiver);
        bootstrap.localAddress(IPAddressUtils.getPort(LOCAL_ADDRESS))
                .group(workerGroup)
                .channel(NioSocketChannel.class)
                .handler(channelInitializer);
        log.info("Client started on {}", LOCAL_ADDRESS);

        InetSocketAddress socketRemoteAddress = IPAddressUtils.splitHostnameAndPortSilently(remoteAddress);
        ChannelFuture channelFuture = bootstrap.connect(socketRemoteAddress);

        CountDownLatch latch = new CountDownLatch(1);
        
        channelFuture.addListener(future -> {
            if (future.isSuccess()) {
                log.debug("Connect to remote server successfully. Remote Address : " + remoteAddress);
            } else {
                log.error("Cannot connect to remote server. Remote Address : " + remoteAddress);
            }
            latch.countDown();
        });

        try {
            latch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage());
        }
        
        return channelInitializer.getBasicClientChannelHandler();
    }

    /**
     * 挑选服务发布者
     * @param serviceName 服务名
     * @param random 随机因子
     * @return
     */
    public BasicClientChannelHandler chooseChannelHandler(String serviceName, int random){
        while(!loadBalanceHandler.hasNext(serviceName)){
            try{
                waitingForChannelHandler();
            }catch(InterruptedException e){
                log.error("Waiting for available node is interrupted!", e);
            }
        }
        return loadBalanceHandler.get(serviceName, random);
    }

    /**
     * 寻找服务发布者
     * @param serviceName 服务名
     * @param address 服务提供者地址
     * @return 若服务端突然短线可能会返回null
     */
    public BasicClientChannelHandler chooseChannelHandler(String serviceName, String address){
        return loadBalanceHandler.get(serviceName, address);
    }

    /**
     * 等待可用的服务提供者
     * @return
     * @throws InterruptedException
     */
    private boolean waitingForChannelHandler() throws InterruptedException{
        lock.lock();
        long timeout = ConfigParser.getOrDefault("client.session.timeout", 5000);
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