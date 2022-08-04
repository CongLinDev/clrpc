package conglin.clrpc.netty;

import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ComponentContextAware;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.Destroyable;
import conglin.clrpc.lifecycle.Initializable;
import conglin.clrpc.lifecycle.ObjectLifecycleUtils;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.loadbalance.DefaultMultiLoadBalancer;
import conglin.clrpc.service.loadbalance.LoadBalancer;
import conglin.clrpc.service.loadbalance.MultiLoadBalancer;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.service.registry.ServiceRegistryFactory;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;
import conglin.clrpc.service.router.Router;
import conglin.clrpc.service.router.RouterCondition;
import conglin.clrpc.service.router.RouterResult;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyRouter implements Router, ComponentContextAware, Initializable, Destroyable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyRouter.class);

    private ServiceRegistryFactory registryFactory;
    private ServiceRegistry serviceRegistry;
    private final MultiLoadBalancer<String, ServiceInstance, Channel> multiLoadBalancer;
    private Bootstrap nettyBootstrap;
    private ComponentContext context;

    public NettyRouter() {
        this.multiLoadBalancer = new DefaultMultiLoadBalancer<String, ServiceInstance, Channel>(this::connectProvider,
                Channel::close, ServiceInstance::match);
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
    public void bindRegistryFactory(ServiceRegistryFactory registryFactory) {
        this.registryFactory = registryFactory;
    }

    @Override
    public void init() {
        assert registryFactory != null;
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        this.serviceRegistry = registryFactory.get(properties);
        assert this.serviceRegistry != null;
        ObjectLifecycleUtils.assemble(this.serviceRegistry, getContext());

        ObjectLifecycleUtils.assemble(multiLoadBalancer, getContext());

        nettyBootstrap = new Bootstrap()
                .group(new NioEventLoopGroup(
                        Integer.parseInt(properties.getProperty("netty.io-thread.number", "4"))))
                .channel(NioSocketChannel.class);
        // .handler(new LoggingHandler(LogLevel.INFO))
        ConsumerInitializer initializer = new ConsumerInitializer(
                context.getWith(ComponentContextEnum.EXECUTOR_PIPELINE),
                context.getWith(ComponentContextEnum.SERIALIZATION_HANDLER),
                context.getWith(ComponentContextEnum.PROTOCOL_DEFINITION));
        nettyBootstrap.handler(initializer);
    }

    @Override
    public RouterResult choose(RouterCondition condition) throws NoAvailableServiceInstancesException {
        String serviceName = condition.getServiceName();

        Pair<ServiceInstance, Channel> pair = multiLoadBalancer.getEntity(serviceName, condition.getRandom(),
                condition.getInstanceCondition(), Channel::isActive);
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
    public Channel connectProvider(ServiceInstance serviceInstance) {
        String remoteAddress = serviceInstance.address();
        try {
            ChannelFuture channelFuture = nettyBootstrap.connect(IPAddressUtils.splitHostAndPort(remoteAddress)).sync();
            if (channelFuture.isSuccess()) {
                LOGGER.debug("Connect to remote provider successfully. Remote Address={}", remoteAddress);
                return channelFuture.channel();
            } else {
                LOGGER.error("Cannot connect to remote provider {}", remoteAddress);
            }
        } catch (UnknownHostException | InterruptedException e) {
            LOGGER.error("Cannot connect to remote provider {}. Cause: {}", remoteAddress, e.getMessage());
        }
        return null;
    }

    /**
     * 取消全部的连接
     */
    private void disconnectAllProviderNode() {
        multiLoadBalancer.forEach(Channel::close);
        multiLoadBalancer.clearLoadBalancer();
    }

    @Override
    public void destroy() {
        ObjectLifecycleUtils.destroy(serviceRegistry);
        if (!multiLoadBalancer.isEmpty()) {
            disconnectAllProviderNode();
            nettyBootstrap.config().group().shutdownGracefully();
            ObjectLifecycleUtils.destroy(multiLoadBalancer);
        }
    }

    @Override
    public void subscribe(ServiceInterface<?> serviceInterface, Class<?> loadBalancerClass) {
        if (!multiLoadBalancer.hasType(serviceInterface.name())) {
            @SuppressWarnings("unchecked")
            LoadBalancer<ServiceInstance, Channel> loadBalancer = ClassUtils.loadObjectByType(loadBalancerClass,
                    LoadBalancer.class);
            if (loadBalancer == null) {
                throw new IllegalArgumentException(
                        "Get LoadBalancer Instance Failed. Class=" + loadBalancerClass.getName());
            }
            multiLoadBalancer.addLoadBalancer(serviceInterface.name(), loadBalancer);
            ObjectLifecycleUtils.assemble(loadBalancer, getContext());
            serviceRegistry.subscribeProvider(serviceInterface, loadBalancer::update);
        }
    }
}
