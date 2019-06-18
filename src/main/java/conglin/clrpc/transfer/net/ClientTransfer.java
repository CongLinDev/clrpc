package conglin.clrpc.transfer.net;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.exception.NoAvailableServerException;
import conglin.clrpc.common.util.net.IPAddressUtil;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.service.discovery.BasicServiceDiscovery;
import conglin.clrpc.service.discovery.ServiceDiscovery;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import conglin.clrpc.transfer.net.handler.ClientChannelInitializer;
import conglin.clrpc.transfer.net.handler.ProtostuffClientChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ClientTransfer {

    private static final Logger log = LoggerFactory.getLogger(ClientTransfer.class);

    private EventLoopGroup workerGroup;
    private ClientServiceHandler serviceHandler;

    
    private CopyOnWriteArrayList<BasicClientChannelHandler> connectedHandlers;
    private Map<InetSocketAddress, BasicClientChannelHandler> connectedServerNodes;

    private ServiceDiscovery serviceDiscovery;

    private ReentrantLock lock;
    private Condition connected;

    private AtomicInteger roundCounter;
    

    public ClientTransfer() {
        connectedHandlers = new CopyOnWriteArrayList<>();
        connectedServerNodes = new ConcurrentHashMap<>();

        lock = new ReentrantLock();
        connected = lock.newCondition();
        roundCounter = new AtomicInteger(0);
    }


    /**
     * 开启传输服务
     * 先检查配置文件中是否有 zookeeper 的url地址
     * 若有，则将zookeeper中的服务加入当前服务。
     * 没有则读取配置文件中的ip进行直连服务
     * @param serviceHandler
     */
    public void start(ClientServiceHandler serviceHandler){
        preStart(serviceHandler);

        String zookeeperAddress = (String)ConfigParser.getInstance().get("zookeeper.discovery.url");
        if(zookeeperAddress != null){
            log.info("Discover zookeeper service url =" + zookeeperAddress);
            this.serviceDiscovery = new BasicServiceDiscovery(this);
        }else{
            start(serviceHandler, new String[0]);
        }
    }

    /**
     * 开启传输服务
     * 直连配置文件中以及用户自定义的服务ip地址
     * @param serviceHandler
     * @param initRemoteAddress 直连地址
     */
    public void start(ClientServiceHandler serviceHandler, String... initRemoteAddress){
        preStart(serviceHandler);

        List<String> configRemoteAddress = ConfigParser.getInstance()
               .getOrDefault("client.connect-url", new ArrayList<String>());
        for(String s: initRemoteAddress){
            configRemoteAddress.add(s);
        }
        updateConnectedServer(configRemoteAddress);
    }


    /**
     * 预启动，为启动做准备
     * @param serviceHandler
     */
    private void preStart(ClientServiceHandler serviceHandler){
        this.serviceHandler = serviceHandler;
        if(workerGroup == null){
            int workerThread = ConfigParser.getInstance().getOrDefault("client.thread.worker", 4);
            workerGroup = new NioEventLoopGroup(workerThread);
        }
    }

    public void stop() throws InterruptedException {

        disconnectAllServerNode();
        signalAvailableChannelHandler();
        
        if(workerGroup != null) workerGroup.shutdownGracefully();
        if(serviceDiscovery != null) serviceDiscovery.stop();
    }

    public void updateConnectedServer(List<String> serverAddress){
        if(serverAddress != null){
            if (serverAddress.size() > 0){
                Set<InetSocketAddress> serverNodeSet = IPAddressUtil.splitHostnameAndPort(serverAddress);

                //添加新节点
                for(final InetSocketAddress address : serverNodeSet){
                    if(!connectedServerNodes.keySet().contains(address)){
                        connectServerNode(address, serviceHandler);
                    }
                }

                //关闭并移除无效节点
                for(BasicClientChannelHandler channelHandler : connectedHandlers){
                    SocketAddress address = channelHandler.getChannel().remoteAddress();
                    if(!serverNodeSet.contains(address)){
                        log.info("Remove invalid server node " + address);
                        BasicClientChannelHandler connectedChannelHandler = connectedServerNodes.get(address);
                        if(connectedChannelHandler != null) connectedChannelHandler.close();

                        connectedServerNodes.remove(address);
                        connectedHandlers.remove(channelHandler);
                    }
                }

            }else{
                log.error("No available server node. All server nodes are down.");
                //关闭并移除所有节点
                disconnectAllServerNode();
                connectedHandlers.clear();
            }
        }
    }


    private void disconnectAllServerNode(){
        for(final BasicClientChannelHandler serverHandler : connectedHandlers){
            SocketAddress address = serverHandler.getChannel().remoteAddress();

            BasicClientChannelHandler handler = connectedServerNodes.get(address);
            connectedServerNodes.remove(handler.getChannel().remoteAddress());
            handler.close(); 
        }
    }


    private void connectServerNode(final InetSocketAddress remoteAddress, ClientServiceHandler serviceHandler){
        serviceHandler.submit(new Runnable(){
            @Override
            public void run() {
                Bootstrap bootstrap = new Bootstrap();

                ClientChannelInitializer channelInitializer = new ProtostuffClientChannelInitializer(serviceHandler);
                bootstrap.group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(channelInitializer);
                
                ChannelFuture channelFuture = bootstrap.connect(remoteAddress);
                channelFuture.addListener(new ChannelFutureListener(){
                
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()){
                            log.debug("Connect to remote server successfully. Remote Address :" + remoteAddress.toString());                            
                            addChannelHandler(channelInitializer.getBasicClientChannelHandler());
                        }
                    }
                });
                        
            }
        });
    }


    private void addChannelHandler(BasicClientChannelHandler channelHandler){
        connectedHandlers.add(channelHandler);
        SocketAddress address = channelHandler.getChannel().remoteAddress();
        connectedServerNodes.put((InetSocketAddress)address, channelHandler);
        signalAvailableChannelHandler();
    }

    private void signalAvailableChannelHandler(){
        lock.lock();
        try{
            connected.signalAll();
        }finally{
            lock.unlock();
        }
    }

    private boolean waitingForChannelHandler() throws InterruptedException{
        lock.lock();
        long timeout = ConfigParser.getInstance().getOrDefault("client.session.timeout", 5000);
        try{
            return connected.await(timeout,TimeUnit.MILLISECONDS);
        } finally {
            lock.unlock();
        }
    }

    public BasicClientChannelHandler chooseChannelHandler(){
        while(connectedHandlers.size() == 0){
            try{
                waitingForChannelHandler();
            }catch(InterruptedException e){
                log.error("Waiting for available node is interrupted!", e);
                throw new NoAvailableServerException();
            }
        }

        int index = roundCounter.getAndIncrement() % connectedHandlers.size();
        return connectedHandlers.get(index);
    }

}