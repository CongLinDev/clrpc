package conglin.clrpc.transport;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.loadbalance.ConsistentHashLoadBalancer;
import conglin.clrpc.common.loadbalance.LoadBalancer;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.executor.BasicConsumerServiceExecutor;
import conglin.clrpc.transport.chooser.ProviderChooser;
import conglin.clrpc.transport.chooser.ProviderChooserAdapter;
import conglin.clrpc.transport.handler.ConsumerChannelInitializer;
import conglin.clrpc.transport.message.BasicRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ConsumerTransfer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerTransfer.class);

    private EventLoopGroup workerGroup;

    private final ReentrantLock lock;
    private final Condition connected;
    private long timeoutForWait; // 挑选服务提供者超时等待时间 单位是ms

    private ConsumerContext context;

    private LoadBalancer<String, String, Channel> loadBalancer;

    public ConsumerTransfer() {
        loadBalancer = new ConsistentHashLoadBalancer<>(
                (addr, ch) -> ((InetSocketAddress) ch.remoteAddress()).toString().compareTo(addr) == 0);

        lock = new ReentrantLock();
        connected = lock.newCondition();
    }

    /**
     * 开启传输服务
     * 
     * @param context 上下文
     */
    public void start(ConsumerContext context) {
        this.context = context;
        initContext(context);

        PropertyConfigurer configurer = context.getPropertyConfigurer();

        timeoutForWait = configurer.getOrDefault("consumer.wait-time", 5000);

        int workerThread = configurer.getOrDefault("consumer.thread.worker", 4);
        workerGroup = new NioEventLoopGroup(workerThread);
    }

    /**
     * 初始化上下文
     * 
     * @param context
     */
    protected void initContext(ConsumerContext context) {
        context.setProviderChooser(new DefaultProviderChooser(context.getProviderChooserAdapter()));
        context.setConsumerServiceExecutor(new BasicConsumerServiceExecutor(context));
    }

    /**
     * 停止服务
     */
    public void stop() {
        disconnectAllProviderNode();
        signalAvailableChannelHandler();

        if (workerGroup != null)
            workerGroup.shutdownGracefully();
    }

    /**
     * 更新连接的服务器
     * 
     * @param serviceName     服务名
     * @param providerAddress 服务器地址
     */
    public void updateConnectedProvider(String serviceName, Map<String, String> providerAddress) {
        loadBalancer.update(serviceName, providerAddress, addr -> connectProviderNode(serviceName, addr),
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
     * 
     * @param serviceName
     * @param remoteAddress
     */
    private Channel connectProviderNode(String serviceName, String remoteAddress) {
        Bootstrap bootstrap = new Bootstrap();
        ConsumerChannelInitializer channelInitializer = new ConsumerChannelInitializer(context);
        String localAddress = context.getLocalAddress();

        bootstrap.localAddress(IPAddressUtils.getPort(localAddress)).group(workerGroup).channel(NioSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .handler(channelInitializer);
        LOGGER.info("Consumer starts on {}", localAddress);

        try {
            bootstrap.connect(IPAddressUtils.splitHostAndPort(remoteAddress)).sync();
            LOGGER.debug("Connect to remote provider successfully. Remote Address : " + remoteAddress);
            return channelInitializer.channel();
        } catch (UnknownHostException | InterruptedException e) {
            LOGGER.error("Cannot connect to remote provider. Remote Address : " + remoteAddress, e);
        }
        return null;
    }

    /**
     * 等待可用的服务提供者
     * 
     * @return
     * @throws InterruptedException
     */
    private boolean waitingForChannelHandler() throws InterruptedException {
        lock.lock();
        try {
            LOGGER.debug("Wait for Channel Handler " + timeoutForWait + " mm...");
            return connected.await(timeoutForWait, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 有可用的ChannelHandler存在 唤醒等待的线程
     */
    private void signalAvailableChannelHandler() {
        lock.lock();
        try {
            connected.signalAll();
        } finally {
            lock.unlock();
        }
    }

    class DefaultProviderChooser implements ProviderChooser {

        private final ProviderChooserAdapter adapter;

        DefaultProviderChooser(ProviderChooserAdapter adapter) {
            this.adapter = adapter;
        }

        @Override
        public Channel choose(String serviceName, BasicRequest request) {
            while (!loadBalancer.hasNext(serviceName)) {
                try {
                    waitingForChannelHandler();
                } catch (InterruptedException e) {
                    LOGGER.error("Waiting for available node is interrupted!", e);
                }
            }
            int random = adapter.apply(request);
            return loadBalancer.get(serviceName, random);
        }

        @Override
        public Channel choose(String serviceName, String addition) {
            Channel channel = null;
            int count = 0;
            // 尝试三次，若三次未成功，则放弃
            while ((channel = loadBalancer.get(serviceName, addition)) == null && count < 3) {
                count++;
                try {
                    waitingForChannelHandler();
                } catch (InterruptedException e) {
                    LOGGER.error("Waiting for available node is interrupted!", e);
                }
            }
            return channel;
        }
    }
}