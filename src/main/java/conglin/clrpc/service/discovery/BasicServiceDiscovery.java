package conglin.clrpc.service.discovery;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.zookeeper.ZooKeeperUtils;
import conglin.clrpc.transfer.net.ClientTransfer;
import io.netty.util.internal.ThreadLocalRandom;

/**
 * 服务发现
 * 使用 Zookeeper 作为服务注册
 */

public class BasicServiceDiscovery implements ServiceDiscovery{

    private static final Logger log = LoggerFactory.getLogger(BasicServiceDiscovery.class);

    private final String registryAddress; //服务注册地址
    private ZooKeeper zooKeeper;
    private final String rootPath;

    private volatile List<String> dataList;

    private ClientTransfer clientTransfer;

    private String serviceName;

    public BasicServiceDiscovery(ClientTransfer clientTransfer, String serviceName){
        this.serviceName = serviceName;
        this.clientTransfer = clientTransfer;

        //服务注册地址
        registryAddress = ConfigParser.getOrDefault("zookeeper.discovery.address", "localhost:2181");

        String path = ConfigParser.getOrDefault("zookeeper.discovery.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        //session timeout in milliseconds
        int sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);

        CountDownLatch countDownLatch = new CountDownLatch(1);
        try{
            zooKeeper = new ZooKeeper(registryAddress, sessionTimeout, new Watcher(){
                @Override
                public void process(WatchedEvent event) {
                    if(event.getState() == Event.KeeperState.SyncConnected){
                        countDownLatch.countDown();
                    }
                }
            });
            countDownLatch.await();
        }catch(IOException | InterruptedException e){
            log.error(e.getMessage());
        }
    }

    @Override
    public void init(){
        if(zooKeeper != null){
            registerConsumer(serviceName, ClientTransfer.LOCAL_ADDRESS.toString());
            String absPath = rootPath + "/service/" + serviceName + "/providers";

            ZooKeeperUtils.watchChildrenData(zooKeeper, absPath, 
                data -> {
                BasicServiceDiscovery.this.dataList = data;
                updateConnectedServer();
            });
        }
    }

    /**
     * 更新连接的服务器
     */
    private void updateConnectedServer(){
        clientTransfer.updateConnectedServer(serviceName, dataList);
    }

    @Override
    public String discover() {
        int size = dataList.size();
        if(size == 0) return null;

        if(size == 1){
            return dataList.get(0);
        }else{
            return dataList.get(ThreadLocalRandom.current().nextInt());
        }
    }

    @Override
    public void stop(){
        if(zooKeeper != null){
            try{
                zooKeeper.close();
            }catch(InterruptedException e){
                log.error("ZooKeeper stops with error. " + e.getMessage());
            }
        }
    }

    @Override
    public void registerConsumer(String serviceName, String data) {
        //创建服务节点
        String serviceNode = rootPath + "/service/" + serviceName;
        ZooKeeperUtils.createNode(zooKeeper,serviceNode, serviceName);
        //创建消费者节点
        String absPath = rootPath + "/service/" + serviceName + "/consumers/consumer";
        ZooKeeperUtils.createNode(zooKeeper, absPath, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    
}