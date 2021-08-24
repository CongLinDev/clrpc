package conglin.clrpc.transport;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Predicate;

import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.router.instance.ServiceInstance;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.transport.component.RequestSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.loadbalance.ConsistentHashLoadBalancer;
import conglin.clrpc.common.loadbalance.LoadBalancer;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.transport.component.DefaultRequestSender;
import conglin.clrpc.transport.component.ProviderChooser;
import conglin.clrpc.transport.component.ProviderChooserAdapter;
import conglin.clrpc.transport.handler.ConsumerChannelInitializer;
import conglin.clrpc.transport.message.BasicRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ConsumerTransfer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerTransfer.class);

    private Bootstrap nettyBootstrap;

    private final ReentrantLock lock;
    private final Condition connected;
    private int timeoutForWait; // 挑选服务提供者超时等待时间 单位是ms

    private RpcContext context;

    private final LoadBalancer<String, ServiceInstance, Channel> loadBalancer;

    public ConsumerTransfer() {
        loadBalancer = new ConsistentHashLoadBalancer<>(this::connectProviderNode, this::disconnectProviderNode);

        lock = new ReentrantLock();
        connected = lock.newCondition();
    }

    /**
     * 开启传输服务
     * 
     * @param context 上下文
     */
    public void start(RpcContext context) {
        this.context = context;
        initContext(context);

        PropertyConfigurer configurer = (PropertyConfigurer)context.get(RpcContextEnum.PROPERTY_CONFIGURER);
        timeoutForWait = configurer.getOrDefault("consumer.wait-time", 5000);
        initNettyBootstrap(configurer.getOrDefault("consumer.thread.worker", 4));
    }

    /**
     * 初始化上下文
     * 
     * @param context
     */
    protected void initContext(RpcContext context) {
        ProviderChooserAdapter adapter = (ProviderChooserAdapter) context.get(RpcContextEnum.PROVIDER_CHOOSER_ADAPTER);
        context.put(RpcContextEnum.PROVIDER_CHOOSER, new DefaultProviderChooser(adapter));
        context.put(RpcContextEnum.REQUEST_SENDER, new DefaultRequestSender(context));
    }

    /**
     * 初始化 Netty Bootstrap
     * 
     * @param workerThread
     */
    private void initNettyBootstrap(int workerThread) {
        nettyBootstrap = new Bootstrap();
        nettyBootstrap.group(new NioEventLoopGroup(workerThread)).channel(NioSocketChannel.class);
                // .handler(new LoggingHandler(LogLevel.INFO))
        ConsumerChannelInitializer initializer = new ConsumerChannelInitializer();
        initializer.setContext(context);
        nettyBootstrap.handler(initializer);
    }

    /**
     * 停止服务
     */
    public void stop() {
        disconnectAllProviderNode();
        signalWaitingConsumer();

        nettyBootstrap.config().group().shutdownGracefully();
        ((RequestSender)context.get(RpcContextEnum.REQUEST_SENDER)).shutdown();
    }

    /**
     * 更新连接的服务器
     * 
     * @param serviceName 服务名
     * @param providers   服务提供者
     */
    public void updateConnectedProvider(String serviceName, Collection<ServiceInstance> providers) {
        loadBalancer.update(serviceName, providers);
        signalWaitingConsumer();
    }

    /**
     * 取消连接所有节点的服务器
     */
    private void disconnectAllProviderNode() {
        loadBalancer.forEach(Channel::close);
        loadBalancer.clear();
    }

    /**
     * 连接某个特定的服务提供者
     * 
     * @param serviceName
     * @param serviceInstance
     */
    private Channel connectProviderNode(String serviceName, ServiceInstance serviceInstance) {
        String remoteAddress = serviceInstance.address();
        try {
            ChannelFuture channelFuture = nettyBootstrap.connect(IPAddressUtils.splitHostAndPort(remoteAddress)).sync();
            if (channelFuture.isSuccess()) {
                LOGGER.debug("Connect to remote provider successfully. Remote Address={}", remoteAddress);
                String localAddress = IPAddressUtils
                        .addressString((InetSocketAddress) channelFuture.channel().localAddress());
                ((ServiceRegistry)context.get(RpcContextEnum.SERVICE_REGISTRY)).register(serviceName, localAddress,
                        "{}");
                LOGGER.info("Consumer starts on {}", localAddress);
                return channelFuture.channel();
            } else {
                LOGGER.error("Provider starts failed");
                throw new InterruptedException();
            }
        } catch (UnknownHostException | InterruptedException e) {
            LOGGER.error("Cannot connect to remote provider {}. Cause: {}", remoteAddress, e.getMessage());
        }
        return null;
    }

    /**
     * 取消连接某个服务提供者
     * 
     * @param serviceName
     * @param channel
     */
    private void disconnectProviderNode(String serviceName, Channel channel) {
        ((ServiceRegistry)context.get(RpcContextEnum.SERVICE_REGISTRY)).unregister(serviceName, channel.localAddress().toString());
        channel.close();
    }

    /**
     * 等待可用的服务提供者
     * 
     * @param serviceName
     * @return
     * @throws InterruptedException
     */
    private boolean waitingForAvailableProvider(String serviceName) throws InterruptedException {
        lock.lock();
        try {
            LOGGER.debug("Wait for available service=({}) provider {} ms ...", serviceName, timeoutForWait);
            return connected.await(timeoutForWait, TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    /**
     * 是否需要刷新 Provider
     * 
     * @param serviceName
     * @return
     */
    public boolean needRefresh(String serviceName) {
        return !loadBalancer.hasType(serviceName);
    }

    /**
     * 有可用的ChannelHandler存在 唤醒等待的线程
     */
    private void signalWaitingConsumer() {
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
        public Channel choose(BasicRequest request) {
            int random = adapter.apply(request);
            String serviceName = request.serviceName();
            // 不断尝试
            while (true) {
                Channel channel = loadBalancer.get(serviceName, random);
                if (channel != null)
                    return channel;
                try {
                    waitingForAvailableProvider(serviceName);
                } catch (InterruptedException e) {
                    LOGGER.error("Waiting for available provider is interrupted!", e);
                }
                LOGGER.warn("Waiting for available provider.");
            }
        }

        @Override
        public Channel choose(String serviceName, Predicate<ServiceInstance> instancePredicate) {
            int count = 0;
            // 尝试三次，若三次未成功，则放弃
            while (count < 3) {
                Channel channel = loadBalancer.getKey(serviceName, instancePredicate);
                if (channel != null)
                    return channel;
                try {
                    waitingForAvailableProvider(serviceName);
                } catch (InterruptedException e) {
                    LOGGER.error("Waiting for available provider is interrupted!", e);
                }
                count++;
                LOGGER.warn("Waiting for available provider.");
            }
            return null;
        }
    }
}