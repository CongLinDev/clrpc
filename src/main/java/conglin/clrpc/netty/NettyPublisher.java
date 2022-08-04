package conglin.clrpc.netty;

import java.net.UnknownHostException;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ComponentContextAware;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.Destroyable;
import conglin.clrpc.lifecycle.Initializable;
import conglin.clrpc.lifecycle.ObjectLifecycleUtils;
import conglin.clrpc.service.ServiceObjectHolder;
import conglin.clrpc.service.publisher.Publisher;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.service.registry.ServiceRegistryFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyPublisher implements Publisher, Initializable, ComponentContextAware, Destroyable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyPublisher.class);

    private ServiceRegistry serviceRegistry;
    private ServiceRegistryFactory registryFactory;
    private ComponentContext context;
    private ServerBootstrap nettyBootstrap;

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public ComponentContext getContext() {
        return context;
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

        nettyBootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup(1),
                        new NioEventLoopGroup(
                                Integer.parseInt(properties.getProperty("netty.io-thread.number", "4"))))
                .channel(NioServerSocketChannel.class);
        // .handler(new LoggingHandler(LogLevel.INFO))
        ProviderInitializer initializer = new ProviderInitializer(
                context.getWith(ComponentContextEnum.EXECUTOR_PIPELINE),
                context.getWith(ComponentContextEnum.SERIALIZATION_HANDLER),
                context.getWith(ComponentContextEnum.PROTOCOL_DEFINITION));
        nettyBootstrap.childHandler(initializer).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        String instanceAddress = properties.getProperty("instance.address");
        try {
            ChannelFuture channelFuture = nettyBootstrap.bind(IPAddressUtils.splitHostAndPortResolved(instanceAddress))
                    .sync();
            if (!channelFuture.isSuccess()) {
                LOGGER.error("Provider starts failed", channelFuture.cause());
                throw new RuntimeException(channelFuture.cause());
            }
            ServiceObjectHolder serviceObjectHolder = getContext().getWith(ComponentContextEnum.SERVICE_OBJECT_HOLDER);
            serviceObjectHolder.forEach(serviceRegistry::registerProvider);
            LOGGER.info("Provider starts with {}", instanceAddress);
            channelFuture.channel().closeFuture().sync();
        } catch (UnknownHostException e) {
            LOGGER.error("Cannot resolved address {}.", instanceAddress);
        } catch (InterruptedException e) {
            LOGGER.error("Cannot bind address {}.", instanceAddress);
        }
    }

    @Override
    public void destroy() {
        ServiceObjectHolder serviceObjectHolder = getContext().getWith(ComponentContextEnum.SERVICE_OBJECT_HOLDER);
        serviceObjectHolder.forEach(serviceRegistry::unregisterProvider);
        ObjectLifecycleUtils.destroy(serviceRegistry);
        nettyBootstrap.config().group().shutdownGracefully();
        nettyBootstrap.config().childGroup().shutdownGracefully();
    }
}
