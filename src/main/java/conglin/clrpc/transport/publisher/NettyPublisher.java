package conglin.clrpc.transport.publisher;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.handler.DefaultChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyPublisher implements Publisher, Initializable, ComponentContextAware, Destroyable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyPublisher.class);

    private ServiceRegistry serviceRegistry;

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
    public void init() {
        this.serviceRegistry = getContext().getWith(ComponentContextEnum.SERVICE_REGISTRY);

        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        ObjectLifecycleUtils.assemble(serviceRegistry, getContext());

        nettyBootstrap = new ServerBootstrap()
                .group(new NioEventLoopGroup(1),
                        new NioEventLoopGroup(
                                Integer.parseInt(properties.getProperty("provider.io-thread.number", "4"))))
                .channel(NioServerSocketChannel.class);
        // .handler(new LoggingHandler(LogLevel.INFO))
        DefaultChannelInitializer initializer = new DefaultChannelInitializer();
        ObjectLifecycleUtils.assemble(initializer, getContext());
        nettyBootstrap.childHandler(initializer).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

        String instanceAddress = properties.getProperty("provider.instance.address");
        try {
            ChannelFuture channelFuture = nettyBootstrap.bind(IPAddressUtils.splitHostAndPortResolved(instanceAddress))
                    .sync();
            if (!channelFuture.isSuccess()) {
                LOGGER.error("Provider starts failed", channelFuture.cause());
                throw new RuntimeException(channelFuture.cause());
            }
            Map<String, ServiceObject<?>> serviceObjects = getContext()
                    .getWith(ComponentContextEnum.SERVICE_OBJECT_HOLDER);
            for (ServiceObject<?> serviceObject : serviceObjects.values()) {
                serviceRegistry.registerProvider(serviceObject);
            }
            LOGGER.info("Provider starts with {}", instanceAddress);
            channelFuture.channel().closeFuture().sync();
        } catch (UnknownHostException e) {
            LOGGER.error("Cannot resolved address {}.", instanceAddress);
        } catch (InterruptedException e) {
            LOGGER.error("Cannot bind address {}.", instanceAddress);
        }
    }

    @Override
    public boolean isDestroyed() {
        return true;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        ObjectLifecycleUtils.destroy(serviceRegistry);
        nettyBootstrap.config().group().shutdownGracefully();
        nettyBootstrap.config().childGroup().shutdownGracefully();
    }
}
