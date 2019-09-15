package conglin.clrpc.transfer;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.common.util.IPAddressUtils;
import conglin.clrpc.transfer.handler.ProviderChannelInitializer;
import conglin.clrpc.transfer.receiver.RequestReceiver;
import conglin.clrpc.transfer.sender.ResponseSender;
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

    private final String LOCAL_ADDRESS;

    private ResponseSender sender;
    private RequestReceiver receiver;

    public ProviderTransfer(String localAddress){
        this.LOCAL_ADDRESS = localAddress;
    }
    
    /**
     * 启动Netty
     * @param sender
     * @param receiver 
     * @param preparation 准备工作，其中包含将服务其注册到zookeeper中
     */
    public void start(ResponseSender sender, RequestReceiver receiver, Consumer<String> preparation){
        this.sender = sender;
        this.receiver = receiver;
        int bossThread = ConfigParser.getOrDefault("provider.thread.boss", 1);
        int workerThread = ConfigParser.getOrDefault("provider.thread.worker", 4);
        bossGroup = new NioEventLoopGroup(bossThread);
        workerGroup = new NioEventLoopGroup(workerThread);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            //.handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new ProviderChannelInitializer(sender, receiver))
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        try{
            InetSocketAddress address = IPAddressUtils.splitHostnameAndPortResolved(LOCAL_ADDRESS);
            ChannelFuture channelFuture = bootstrap.bind(address).sync();

            log.info("Provider started on {}", address);
            
            //进行准备工作
            preparation.accept(LOCAL_ADDRESS);

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
        sender.stop();
        receiver.stop();
    }
}