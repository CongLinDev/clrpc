package conglin.clrpc.transport;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.handler.ProviderChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ProviderTransfer {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTransfer.class);

    private ServerBootstrap nettyBootstrap;

    private ProviderContext context;

    /**
     * 启动Netty
     * 
     * @param context 上下文
     */
    public void start(ProviderContext context) {
        this.context = context;
        PropertyConfigurer configurer = context.getPropertyConfigurer();
        initNettyBootstrap(configurer.getOrDefault("provider.thread.boss", 1),
                configurer.getOrDefault("provider.thread.worker", 4));

        InetSocketAddress localAddress = context.getLocalAddress();
        String localAddressString = localAddress.toString();
        try {
            ChannelFuture channelFuture = nettyBootstrap.bind(localAddress).sync();
            if (channelFuture.isSuccess()) {
                context.getServiceRegister().run();
                LOGGER.info("Provider starts on {}", localAddressString);
            } else {
                LOGGER.error("Provider starts failed");
                throw new InterruptedException();
            }
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            LOGGER.error("Cannot bind local address. {}", localAddressString);
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
                .channel(NioServerSocketChannel.class)
                // .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ProviderChannelInitializer(context)).option(ChannelOption.SO_BACKLOG, 128)
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