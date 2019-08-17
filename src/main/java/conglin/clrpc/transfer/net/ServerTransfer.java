package conglin.clrpc.transfer.net;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.net.IPAddressUtils;
import conglin.clrpc.transfer.net.handler.BasicServerChannelInitializer;
import conglin.clrpc.transfer.net.receiver.RequestReceiver;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class ServerTransfer{
    private static final Logger log = LoggerFactory.getLogger(ServerTransfer.class);

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final String serverAddress;

    private RequestReceiver receiver;

    public ServerTransfer(){
        this.serverAddress = ConfigParser.getOrDefault("server.address", "localhost:5100");
    }

    // public ServerTransfer(String serverAddress){
    //     this.serverAddress = serverAddress;
    // }
    
    /**
     * 启动Netty
     * @param receiver 
     * @param preparation 准备工作，其中包含将服务其注册到zookeeper中
     */
    public void start(RequestReceiver receiver, Consumer<String> preparation){
        this.receiver = receiver;
        int bossThread = ConfigParser.getOrDefault("server.thread.boss", 1);
        int workerThread = ConfigParser.getOrDefault("server.thread.worker", 4);
        bossGroup = new NioEventLoopGroup(bossThread);
        workerGroup = new NioEventLoopGroup(workerThread);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            //.handler(new LoggingHandler(LogLevel.INFO))
            .childHandler(new BasicServerChannelInitializer(receiver))
            .option(ChannelOption.SO_BACKLOG, 128)
            .childOption(ChannelOption.SO_KEEPALIVE, true);

        try{
            InetSocketAddress address = IPAddressUtils.splitHostnameAndPortResolved(serverAddress);
            ChannelFuture channelFuture = bootstrap.bind(address).sync();

            log.info("Server started on {}", address);
            
            //进行准备工作
            preparation.accept(serverAddress);

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
        receiver.stop();
    }
}