package conglin.clrpc.transport;

import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.router.instance.ServiceInstanceCodec;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.transport.handler.DefaultChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.Map;
import java.util.Properties;

public class ProviderTransfer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTransfer.class);

    private ServerBootstrap nettyBootstrap;

    private RpcContext context;

    /**
     * 启动Netty
     *
     * @param context 上下文
     */
    public void start(RpcContext context) {
        this.context = context;
        Properties properties = context.getWith(RpcContextEnum.PROPERTIES);
        initNettyBootstrap(Integer.parseInt(properties.getProperty("provider.thread.boss", "1")),
                Integer.parseInt(properties.getProperty("provider.thread.worker", "4")));
        int servicePort = Integer.parseInt(properties.getProperty("provider.port", "0"));
        try {
            ChannelFuture channelFuture = nettyBootstrap.bind(IPAddressUtils.localAddress(servicePort)).sync();
            if (channelFuture.isSuccess()) {
                String localAddress = IPAddressUtils
                        .addressString((InetSocketAddress) channelFuture.channel().localAddress());
                ServiceRegistry serviceRegistry = context.getWith(RpcContextEnum.SERVICE_REGISTRY);
                Map<String, ServiceObject> serviceObjects = context.getWith(RpcContextEnum.SERVICE_OBJECT_HOLDER);
                ServiceInstanceCodec serviceInstanceCodec = context.getWith(RpcContextEnum.SERVICE_INSTANCE_CODEC);
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

    /**
     * 初始化 Netty Bootstrap
     *
     * @param bossThread
     * @param workerThread
     */
    private void initNettyBootstrap(int bossThread, int workerThread) {
        nettyBootstrap = new ServerBootstrap();
        nettyBootstrap.group(new NioEventLoopGroup(bossThread), new NioEventLoopGroup(workerThread))
                .channel(NioServerSocketChannel.class);
        // .handler(new LoggingHandler(LogLevel.INFO))
        DefaultChannelInitializer initializer = new DefaultChannelInitializer();
        initializer.setContext(context);
        initializer.init();
        nettyBootstrap.childHandler(initializer).option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
    }

    /**
     * 关闭Netty
     */
    public void stop() {
        nettyBootstrap.config().group().shutdownGracefully();
        nettyBootstrap.config().childGroup().shutdownGracefully();
    }
}