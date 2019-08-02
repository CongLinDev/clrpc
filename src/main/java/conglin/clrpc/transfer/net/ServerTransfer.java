package conglin.clrpc.transfer.net;

import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.net.IPAddressUtil;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.handler.BasicServerChannelInitializer;
import conglin.clrpc.transfer.net.receiver.BasicRequestReceiver;
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
        this(ConfigParser.getOrDefault("server.address", "localhost:5100"));
    }

    public ServerTransfer(String serverAddress){
        this.serverAddress = serverAddress;
    }

    /**
     * 启动Netty 并将其注册到zookeeper中
     * @param serviceHandler
     */
    public void start(ServerServiceHandler serviceHandler){

        this.receiver = new BasicRequestReceiver();

        String receiverClassName = ConfigParser.getOrDefault("server.request-receiver", "conglin.clrpc.transfer.net.receiver.BasicRequestReceiver");

        try {
            this.receiver = (RequestReceiver) Class.forName(receiverClassName)
                    .getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.net.receiver.BasicRequestReceiver' rather than "
                    + receiverClassName);
        }finally{
            // 如果类名错误，则默认加载 {@link conglin.clrpc.transfer.net.receiver.BasicRequestReceiver}
            this.receiver = (this.receiver == null) ? new BasicRequestReceiver() : this.receiver;
        }
        receiver.init(serviceHandler);

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
            InetSocketAddress address= IPAddressUtil.splitHostnameAndPort(serverAddress);

            ChannelFuture channelFuture = bootstrap.bind(address.getAddress(), address.getPort()).sync();
            log.info("Server started on {}", address);
            
            //注册到zookeeper
            serviceHandler.registerService(serverAddress);

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