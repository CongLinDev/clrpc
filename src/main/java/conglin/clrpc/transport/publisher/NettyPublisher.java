package conglin.clrpc.transport.publisher;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.instance.ServiceInstanceCodec;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.handler.DefaultChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyPublisher implements Publisher, Initializable, ContextAware, Destroyable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NettyPublisher.class);

    private final ServiceRegistry serviceRegistry;
    
    private RpcContext context;
    private ServerBootstrap nettyBootstrap;
    public NettyPublisher(ServiceRegistry serviceRegistry) {
        this.serviceRegistry = serviceRegistry;
    }
    @Override
    public void setContext(RpcContext context) {
        this.context = context;
    }
    @Override
    public RpcContext getContext() {
        return context;
    }
    @Override
    public void init() {
        Properties properties = getContext().getWith(RpcContextEnum.PROPERTIES);
        
        nettyBootstrap = new ServerBootstrap();
        nettyBootstrap.group(new NioEventLoopGroup(Integer.parseInt(properties.getProperty("provider.thread.boss", "1"))),
                            new NioEventLoopGroup(Integer.parseInt(properties.getProperty("provider.thread.worker", "4"))))
                    .channel(NioServerSocketChannel.class);
        // .handler(new LoggingHandler(LogLevel.INFO))
        DefaultChannelInitializer initializer = new DefaultChannelInitializer();
        ObjectLifecycleUtils.assemble(initializer, getContext());
        nettyBootstrap.childHandler(initializer).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
                
        int servicePort = Integer.parseInt(properties.getProperty("provider.port", "0"));
        try {
            ChannelFuture channelFuture = nettyBootstrap.bind(IPAddressUtils.localAddress(servicePort)).sync();
            if (channelFuture.isSuccess()) {
                String localAddress = IPAddressUtils
                        .addressString((InetSocketAddress) channelFuture.channel().localAddress());
                Map<String, ServiceObject> serviceObjects = getContext().getWith(RpcContextEnum.SERVICE_OBJECT_HOLDER);
                ServiceInstanceCodec serviceInstanceCodec = getContext().getWith(RpcContextEnum.SERVICE_INSTANCE_CODEC);
                serviceObjects.values().forEach(serviceObject -> {
                    String instanceInfo = serviceInstanceCodec.toString(serviceObject, localAddress);
                    serviceRegistry.register(serviceObject.name(), localAddress, instanceInfo);
                });
                LOGGER.info("Provider starts on {}", localAddress);
            } else {
                LOGGER.error("Provider starts failed");
                throw new InterruptedException();
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("Cannot bind port {}.", servicePort);
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
