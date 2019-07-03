package conglin.clrpc.service.discovery;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.zookeeper.NodeManager;
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

    public BasicServiceDiscovery(ClientTransfer clientTransfer, String serviceName){

        this.clientTransfer = clientTransfer;

        //服务注册地址
        registryAddress = ConfigParser.getInstance().getOrDefault("zookeeper.discovery.url", "localhost:2181");

        String path = ConfigParser.getInstance().getOrDefault("zookeeper.discovery.root_path", "/clrpc");
        rootPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        //session timeout in milliseconds
        int sessionTimeout = ConfigParser.getInstance().getOrDefault("zookeeper.session.timeout", 5000);

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
            log.error("", e);
        }

        if(zooKeeper != null){
            // registerConsumer(serviceName, )
            String absPath = rootPath + "/service/" + serviceName + "/providers";
            watchNode(zooKeeper, absPath);
        }
    }

    /**
     * 监视结点
     * @param keeper
     */
    private void watchNode(final ZooKeeper keeper, String path){
        try{
            List<String> nodeList = keeper.getChildren(path, new Watcher(){
                @Override
                public void process(WatchedEvent event) {
                    if(event.getType() == Event.EventType.NodeChildrenChanged){
                        watchNode(keeper, path);
                    }
                }
            });

            List<String> list = new ArrayList<>();
            for(String node : nodeList){
                byte[] bytes = keeper.getData(path + "/" + node, false, null);
                if(bytes.length > 0)
                    list.add(new String(bytes));
            }
            this.dataList = list;
            log.debug("node data: {}", list);

        }catch(KeeperException | InterruptedException e){
            log.error(e.getMessage());
        }
        
        log.debug("Service discovery triggered updating connected server node.");
        updateConnectedServer();
    }

    /**
     * 更新连接的服务器
     */
    private void updateConnectedServer(){
        clientTransfer.updateConnectedServer(dataList);
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
        NodeManager.createNode(zooKeeper,serviceNode, serviceName);
        //创建消费者节点
        String absPath = rootPath + "/service/" + serviceName + "/consumers/consumer";
        NodeManager.createNode(zooKeeper, absPath, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    
}