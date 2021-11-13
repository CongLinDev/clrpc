package conglin.clrpc.transport;

import conglin.clrpc.common.loadbalance.ConsistentHashLoadBalancer;
import conglin.clrpc.common.loadbalance.LoadBalancer;
import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.router.ProviderRouter;
import conglin.clrpc.router.instance.ServiceInstance;
import conglin.clrpc.router.instance.ServiceInstanceCodec;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.transport.component.DefaultRequestSender;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.handler.DefaultChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

public class ConsumerTransfer {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerTransfer.class);

    private Bootstrap nettyBootstrap;
    private RpcContext context;

    private final LoadBalancer<String, ServiceInstance, Channel> loadBalancer;

    public ConsumerTransfer() {
        loadBalancer = new ConsistentHashLoadBalancer<>(this::connectProviderNode, this::disconnectProviderNode);
    }

    /**
     * 开启传输服务
     *
     * @param context 上下文
     */
    public void start(RpcContext context) {
        this.context = context;
        initContext(context);

        Properties properties = context.getWith(RpcContextEnum.PROPERTIES);
        initNettyBootstrap(Integer.parseInt(properties.getProperty("consumer.thread.worker", "4")));
    }

    /**
     * 初始化上下文
     *
     * @param context
     */
    protected void initContext(RpcContext context) {
        context.put(RpcContextEnum.PROVIDER_ROUTER, new ProviderRouter(loadBalancer));
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
        DefaultChannelInitializer initializer = new DefaultChannelInitializer();
        initializer.setContext(context);
        initializer.init();
        nettyBootstrap.handler(initializer);
    }

    /**
     * 停止服务
     */
    public void stop() {
        disconnectAllProviderNode();
        ProviderRouter router = context.getWith(RpcContextEnum.PROVIDER_ROUTER);
        router.refresh();

        nettyBootstrap.config().group().shutdownGracefully();
        ((RequestSender) context.get(RpcContextEnum.REQUEST_SENDER)).shutdown();
    }

    /**
     * 更新连接的服务器
     *
     * @param serviceName 服务名
     * @param providers   服务提供者
     */
    public void updateConnectedProvider(String serviceName, Collection<Pair<String, String>> providers) {
        ServiceInstanceCodec serviceInstanceCodec = context.getWith(RpcContextEnum.SERVICE_INSTANCE_CODEC);
        Collection<ServiceInstance> instances = providers.stream().map(pair-> serviceInstanceCodec.fromContent(pair.getSecond())).collect(Collectors.toList());
        loadBalancer.update(serviceName, instances);
        ProviderRouter router = context.getWith(RpcContextEnum.PROVIDER_ROUTER);
        router.refresh();
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
                ((ServiceRegistry) context.get(RpcContextEnum.SERVICE_REGISTRY)).register(serviceName, localAddress,
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
        ((ServiceRegistry) context.get(RpcContextEnum.SERVICE_REGISTRY)).unregister(serviceName,
                channel.localAddress().toString());
        channel.close();
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
}