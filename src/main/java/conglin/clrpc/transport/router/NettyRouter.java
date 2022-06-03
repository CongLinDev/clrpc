package conglin.clrpc.transport.router;

import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.loadbalance.DefaultMultiLoadBalancer;
import conglin.clrpc.common.loadbalance.LoadBalancer;
import conglin.clrpc.common.loadbalance.MultiLoadBalancer;
import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.handler.DefaultChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class NettyRouter implements Router, ComponentContextAware, Initializable, Destroyable {
    private final static Logger LOGGER = LoggerFactory.getLogger(NettyRouter.class);

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
    public void init() {
        serviceRegistry = getContext().getWith(ComponentContextEnum.SERVICE_REGISTRY);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);

        ObjectLifecycleUtils.assemble(multiLoadBalancer, getContext());
        ObjectLifecycleUtils.assemble(serviceRegistry, getContext());

        nettyBootstrap = new Bootstrap()
                .group(new NioEventLoopGroup(
                        Integer.parseInt(properties.getProperty("consumer.io-thread.number", "4"))))
                .channel(NioSocketChannel.class);
        // .handler(new LoggingHandler(LogLevel.INFO))
        DefaultChannelInitializer initializer = new DefaultChannelInitializer();
        ObjectLifecycleUtils.assemble(initializer, getContext());
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
                LOGGER.error("Provider starts failed");
                throw new InterruptedException();
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
        if (!multiLoadBalancer.isEmpty()) {
            ObjectLifecycleUtils.destroy(serviceRegistry);
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
