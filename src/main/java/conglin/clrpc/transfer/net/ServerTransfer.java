package conglin.clrpc.transfer.net;

import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.net.IPAddressUtil;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.handler.ProtostuffServerChannelInitializer;
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

    private String serverAddress;
    

    public ServerTransfer(){
        this.serverAddress = ConfigParser.getInstance().getOrDefault("server.url", "localhost:5000");
    }

    public ServerTransfer(String serverAddress){
        this.serverAddress = serverAddress;
    }

    /**
     * 启动Netty 并将其注册到zookeeper中
     * 默认使用 Protostuff
     * @param serviceHandler
     */
    public void start(ServerServiceHandler serviceHandler){
        if(bossGroup == null && workerGroup == null){
            int bossThread = ConfigParser.getInstance().getOrDefault("server.thread.boss", 1);
            int workerThread = ConfigParser.getInstance().getOrDefault("server.thread.worker", 4);
            bossGroup = new NioEventLoopGroup(bossThread);
            workerGroup = new NioEventLoopGroup(workerThread);

            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                //.handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ProtostuffServerChannelInitializer(serviceHandler))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true);

            try{
                InetSocketAddress address= IPAddressUtil.splitHostnameAndPort(serverAddress);

                ChannelFuture channelFuture = bootstrap.bind(address.getAddress(), address.getPort()).sync();
                log.info("Server started on {}", address);

                //注册到zookeeper
                serviceHandler.registerService(serverAddress);

                channelFuture.channel().closeFuture().sync();
            }catch(UnknownHostException | InterruptedException e){
                log.error("", e);
            }finally{
                stop();
            }
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