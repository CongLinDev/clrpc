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

    public static final String ANONYMOUS_SERVICE_NAME = "AnonymousService";

    public static final InetSocketAddress LOCAL_ADDRESS = IPAddressUtil
            .splitHostnameAndPortSilently(ConfigParser.getInstance().getOrDefault("client.address", "localhost:5200"));

    private EventLoopGroup workerGroup;
    private ClientServiceHandler serviceHandler;

    private Map<String, ClientTransferNode> transferNodes;

    public ClientTransfer() {
        transferNodes = new ConcurrentHashMap<>();
    }

    /**
     * 开启传输服务 先检查配置文件中是否有 zookeeper 的url地址 若有，则将zookeeper中的服务加入当前服务。
     * 没有则读取配置文件中的ip进行直连服务
     * 
     * @param serviceHandler
     */
    public void start(ClientServiceHandler serviceHandler) {
        preStart(serviceHandler);

        String zookeeperAddress = (String) ConfigParser.getInstance().get("zookeeper.discovery.address");
        if (zookeeperAddress != null) {
            log.debug("Discovering zookeeper service address = " + zookeeperAddress);
        } else {
            start(serviceHandler, new String[0]);
        }
    }

    /**
     * 开启传输服务 直连配置文件中以及用户自定义的服务ip地址
     * 
     * @param serviceHandler
     * @param initRemoteAddress 直连地址
     */
    public void start(ClientServiceHandler serviceHandler, String... initRemoteAddress) {
        preStart(serviceHandler);

        List<String> configRemoteAddress = ConfigParser.getInstance().getOrDefault("client.connect-address",
                new ArrayList<String>());
        for (String s : initRemoteAddress) {
            configRemoteAddress.add(s);
        }
        updateConnectedServer(ANONYMOUS_SERVICE_NAME, configRemoteAddress);
    }

    /**
     * 在 ZooKeeper中寻找服务提供者
     * 
     * @param <T>
     * @param interfaceClass 提供该服务的类
     */
    public <T> void findService(Class<T> interfaceClass) {
        ClientTransferNode node = new ClientTransferNode(interfaceClass.getSimpleName());
        transferNodes.put(interfaceClass.getSimpleName(), node);
        node.init();
    }

    /**
     * 预启动，为启动做准备
     * 
     * @param serviceHandler
     */
    private void preStart(ClientServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
        if (workerGroup == null) {
            int workerThread = ConfigParser.getInstance().getOrDefault("client.thread.worker", 4);
            workerGroup = new NioEventLoopGroup(workerThread);
        }
    }

    /**
     * 停止服务
     */
    public void stop() {
        disconnectAllServerNode();
        signalAvailableChannelHandler();

        if (workerGroup != null)
            workerGroup.shutdownGracefully();

        transferNodes.values().forEach(node -> node.stop());
    }

    /**
     * 更新连接的服务器
     * 
     * @param serviceName   服务名
     * @param serverAddress 服务器地址
     */
    public void updateConnectedServer(String serviceName, List<String> serverAddress) {
        if (transferNodes.containsKey(serviceName)) {
            log.debug("Starting to update connected provider whose service name is " + serviceName);
            transferNodes.get(serviceName).updateConnectedServer(serverAddress);
        }
    }

    /**
     * 取消连接所有节点的服务器
     */
    private void disconnectAllServerNode() {
        transferNodes.values().forEach(node -> node.disconnectAllServerNode());
    }

    private void signalAvailableChannelHandler() {
        transferNodes.values().forEach(node -> node.signalAvailableChannelHandler());
    }

    /**
     * 连接某个特定的服务提供者
     */
    private void connectServerNode(String serviceName, final InetSocketAddress remoteAddress) {
        serviceHandler.submit(new Runnable() {
            @Override
            public void run() {
                Bootstrap bootstrap = new Bootstrap();
                ClientChannelInitializer channelInitializer = new ProtostuffClientChannelInitializer(serviceHandler);
                bootstrap.localAddress(LOCAL_ADDRESS.getPort())
                        .group(workerGroup)
                        .channel(NioSocketChannel.class)
                        .handler(channelInitializer);
                log.info("Client started on {}", LOCAL_ADDRESS.toString());
                ChannelFuture channelFuture = bootstrap.connect(remoteAddress);

                channelFuture.addListener(new ChannelFutureListener(){
                
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        if(future.isSuccess()){
                            log.debug("Connect to remote server successfully. Remote Address : " + remoteAddress.toString());
                            transferNodes.get(serviceName).
                                addChannelHandler(channelInitializer.getBasicClientChannelHandler());
                        }
                    }
                });
            }
        });
    }

    /**
     * 挑选服务发布者
     * @param serviceName
     * @return
     */
    public BasicClientChannelHandler chooseChannelHandler(String serviceName){
        if(transferNodes.containsKey(serviceName)){
            return transferNodes.get(serviceName).chooseChannelHandler();
        }else if(transferNodes.containsKey(ANONYMOUS_SERVICE_NAME)){
            return transferNodes.get(ANONYMOUS_SERVICE_NAME).chooseChannelHandler();
        }else{
            return null;
        }
    }



    /**
     * 每个 ClientTransferNode 节点都持有一个指定服务名称的服务
     * 该服务由一个或多个服务器进行提供。
     * 客户端请求该服务时，目前使用轮询的办法进行处理
     */

    class ClientTransferNode {
    
        private CopyOnWriteArrayList<BasicClientChannelHandler> connectedHandlers;
        private Map<InetSocketAddress, BasicClientChannelHandler> connectedServerNodes;
    
        private ReentrantLock lock;
        private Condition connected;
    
        private AtomicInteger roundCounter;
    
        private ServiceDiscovery serviceDiscovery;
    
        private String serviceName;
    
        public ClientTransferNode(String serviceName) {
            this.serviceName = serviceName;
    
            connectedHandlers = new CopyOnWriteArrayList<>();
            connectedServerNodes = new ConcurrentHashMap<>();
    
            lock = new ReentrantLock();
            connected = lock.newCondition();
            roundCounter = new AtomicInteger(0);
    
            serviceDiscovery = new BasicServiceDiscovery(ClientTransfer.this, serviceName);
        }

        /**
         * 初始化
         */
        public void init(){
            serviceDiscovery.init();
        }
    
        /**
         * 更新服务地址
         * @param serverAddress
         */
        public void updateConnectedServer(List<String> serverAddress) {
            if (serverAddress != null) {
                if (serverAddress.size() > 0) {
                    Set<InetSocketAddress> serverNodeSet = IPAddressUtil.splitHostnameAndPort(serverAddress);
    
                    // 添加新节点
                    for (final InetSocketAddress address : serverNodeSet) {
                        if (!connectedServerNodes.keySet().contains(address)) {
                            log.info("Connecting server address = " + address);
                            connectServerNode(serviceName, address);
                        }
                    }
    
                    // 关闭并移除无效节点
                    for (BasicClientChannelHandler channelHandler : connectedHandlers) {
                        SocketAddress address = channelHandler.getChannel().remoteAddress();
                        if (!serverNodeSet.contains(address)) {
                            log.info("Remove invalid server node " + address);
                            BasicClientChannelHandler connectedChannelHandler = connectedServerNodes.get(address);
                            if (connectedChannelHandler != null)
                                connectedChannelHandler.close();
    
                            connectedServerNodes.remove(address);
                            connectedHandlers.remove(channelHandler);
                        }
                    }
    
                } else {
                    log.error("No available server node. All server nodes are down.");
                    // 关闭并移除所有节点
                    disconnectAllServerNode();
                    connectedHandlers.clear();
                }
            }
        }
    
        /**
         * 取消连接所有的服务提供者
         */
        public void disconnectAllServerNode(){
            for(final BasicClientChannelHandler serverHandler : connectedHandlers){
                SocketAddress address = serverHandler.getChannel().remoteAddress();
    
                BasicClientChannelHandler handler = connectedServerNodes.get(address);
                connectedServerNodes.remove(handler.getChannel().remoteAddress());
                handler.close(); 
            }
        }
    
        /**
         * 添加服务提供者的Channel
         * @param channelHandler
         */
        public void addChannelHandler(BasicClientChannelHandler channelHandler){
            connectedHandlers.add(channelHandler);
            SocketAddress address = channelHandler.getChannel().remoteAddress();
            connectedServerNodes.put((InetSocketAddress)address, channelHandler);
            signalAvailableChannelHandler();
        }
    
        public void signalAvailableChannelHandler(){
            lock.lock();
            try{
                connected.signalAll();
            }finally{
                lock.unlock();
            }
        }
    
        /**
         * 等待可用的服务提供者
         * @return
         * @throws InterruptedException
         */
        private boolean waitingForChannelHandler() throws InterruptedException{
            lock.lock();
            long timeout = ConfigParser.getInstance().getOrDefault("client.session.timeout", 5000);
            try{
                return connected.await(timeout,TimeUnit.MILLISECONDS);
            } finally {
                lock.unlock();
            }
        }
    
        /**
         * 选择一个服务提供者
         * 目前采用轮询的办法进行选择
         */
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
    
        /**
         * 停止服务
         */
        public void stop(){
            if(serviceDiscovery != null)
                serviceDiscovery.stop();
        }
    }
}