package conglin.clrpc.service.discovery;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.zookeeper.ZooKeeperUtils;

/**
 * 服务发现
 * 使用 Zookeeper 作为服务注册
 */

public class BasicServiceDiscovery implements ServiceDiscovery{

    private static final Logger log = LoggerFactory.getLogger(BasicServiceDiscovery.class);
    private final String registryAddress; //服务注册地址
    private ZooKeeper zooKeeper;
    private final String rootPath;

    public BasicServiceDiscovery(){

        String path = ConfigParser.getOrDefault("zookeeper.discovery.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        //服务注册地址
        registryAddress = ConfigParser.getOrDefault("zookeeper.discovery.address", "localhost:2181");
        log.debug("Discovering zookeeper service address = " + registryAddress);
        //session timeout in milliseconds
        int sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);

        zooKeeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);
    }

    @Override
    public void discover(String serviceName, BiConsumer<String, List<String>> updateMethod) {
        if(zooKeeper != null){
            String absPath = rootPath + "/service/" + serviceName + "/providers";
            ZooKeeperUtils.watchChildrenData(zooKeeper, absPath, 
                list -> updateMethod.accept(serviceName, list));
        }
    }

    @Override
    public void stop(){
        try{
            ZooKeeperUtils.disconnectZooKeeper(zooKeeper);
            log.debug("Service discovery shuted down.");
        }catch(InterruptedException e){
            log.error(e.getMessage());
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