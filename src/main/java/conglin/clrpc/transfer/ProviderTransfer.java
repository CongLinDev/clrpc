package conglin.clrpc.transfer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transfer.handler.ProviderChannelInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ProviderTransfer{
    private static final Logger log = LoggerFactory.getLogger(ProviderTransfer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    /**
     * 启动Netty
     * @param context 上下文
     */
    public void start(ProviderContext context){
        PropertyConfigurer configurer = context.getPropertyConfigurer();
        int bossThread = configurer.getOrDefault("provider.thread.boss", 1);
        int workerThread = configurer.getOrDefault("provider.thread.worker", 4);
        bossGroup = new NioEventLoopGroup(bossThread);
        workerGroup = new NioEventLoopGroup(workerThread);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            //.handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ProviderChannelInitializer(context))
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        try{
            InetSocketAddress address = IPAddressUtils.splitHostnameAndPortResolved(context.getLocalAddress());
            ChannelFuture channelFuture = bootstrap.bind(address).sync();

            log.info("Provider started on {}", address);
            
            //进行准备工作
            context.getServiceRegister().accept(context.getLocalAddress());

            channelFuture.channel().closeFuture().sync();
        }catch(UnknownHostException | InterruptedException e){
            log.error(e.getMessage());
        }finally{
            stop();
        }
    }

    /**
     * 关闭Netty
     */
    public void stop(){
        if(bossGroup != null) bossGroup.shutdownGracefully();
        if(workerGroup != null) workerGroup.shutdownGracefully();
    }
}