package conglin.clrpc.transport.router;

import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Properties;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.loadbalance.ConsistentHashLoadBalancer;
import conglin.clrpc.common.loadbalance.LoadBalancer;
import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.registry.ServiceDiscovery;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.codec.ServiceInstanceCodec;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.handler.DefaultChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyRouter implements Router, ComponentContextAware, Initializable, Destroyable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyRouter.class);

    private final ServiceDiscovery serviceDiscovery;
    private final LoadBalancer<String, ServiceInstance, Channel> loadBalancer;
    private Bootstrap nettyBootstrap;
    private ServiceInstanceCodec serviceInstanceCodec;
    private ComponentContext context;

    public NettyRouter(ServiceDiscovery serviceDiscovery) {
        this.serviceDiscovery = serviceDiscovery;
        this.loadBalancer = new ConsistentHashLoadBalancer<String, ServiceInstance, Channel>(this::connectProvider,
                this::disconnectProvider);
    }

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public void init() {
        ObjectLifecycleUtils.assemble(loadBalancer, getContext());
        ObjectLifecycleUtils.assemble(serviceDiscovery, getContext());
        serviceInstanceCodec = getContext().getWith(ComponentContextEnum.SERVICE_INSTANCE_CODEC);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        nettyBootstrap = new Bootstrap()
                .group(new NioEventLoopGroup(Integer.parseInt(properties.getProperty("consumer.io-thread.number", "4"))))
                .channel(NioSocketChannel.class);
        // .handler(new LoggingHandler(LogLevel.INFO))
        DefaultChannelInitializer initializer = new DefaultChannelInitializer();
        ObjectLifecycleUtils.assemble(initializer, getContext());
        nettyBootstrap.handler(initializer);
    }

    @Override
    public RouterResult choose(RouterCondition condition) throws NoAvailableServiceInstancesException {
        String serviceName = condition.getServiceName();

        Pair<ServiceInstance, Channel> pair = loadBalancer.getEntity(serviceName, condition.getRandom(),
                condition.getInstanceCondition());
        if (pair == null)
            throw new NoAvailableServiceInstancesException(condition);
        return new RouterResult(pair.getFirst(), pair.getSecond()::writeAndFlush);
    }

    /**
     * 连接某个特定的服务提供者
     *
     * @param serviceName
     * @param serviceInstance
     */
    public Channel connectProvider(String serviceName, ServiceInstance serviceInstance) {
        String remoteAddress = serviceInstance.address();
        try {
            ChannelFuture channelFuture = nettyBootstrap.connect(IPAddressUtils.splitHostAndPort(remoteAddress)).sync();
            if (channelFuture.isSuccess()) {
                LOGGER.debug("Connect to remote provider successfully. Remote Address={}", remoteAddress);
                Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
                String instanceId = properties.getProperty("consumer.instance.id");
                serviceDiscovery.register(serviceName, instanceId, "{}");
                LOGGER.info("Consumer {} starts.", instanceId);
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
    public void disconnectProvider(String serviceName, Channel channel) {
        serviceDiscovery.unregister(serviceName, channel.localAddress().toString());
        channel.close();
    }

    /**
     * 取消全部的连接
     */
    private void disconnectAllProviderNode() {
        loadBalancer.forEach(Channel::close);
        loadBalancer.clear();
    }

    @Override
    public void destroy() throws DestroyFailedException {
        ObjectLifecycleUtils.destroy(serviceDiscovery);
        disconnectAllProviderNode();
        nettyBootstrap.config().group().shutdownGracefully();
        ObjectLifecycleUtils.destroy(loadBalancer);
    }

    @Override
    public boolean isDestroyed() {
        return loadBalancer.isEmpty();
    }

    @Override
    public void subscribe(String serviceName) {
        if (!loadBalancer.hasType(serviceName)) {
            serviceDiscovery.discover(serviceName, this::updateConnectedProvider);
        }
    }

    /**
     * 更新 Connected Provider
     * @param serviceName
     * @param providers
     */
    public void updateConnectedProvider(String serviceName, Collection<Pair<String, String>> providers) {
        Collection<ServiceInstance> instances = providers.stream()
                .map(Pair::getSecond)
                .map(serviceInstanceCodec::fromContent)
                .collect(Collectors.toList());
        loadBalancer.update(serviceName, instances);
    }
}
