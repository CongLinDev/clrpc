package conglin.clrpc.service.discovery;

import java.util.List;
import java.util.function.BiConsumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.util.ZooKeeperUtils;

/**
 * 服务发现
 * 使用 Zookeeper 作为服务注册
 */

public class BasicServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(BasicServiceDiscovery.class);
    private final String registryAddress; //服务注册地址
    private ZooKeeper zooKeeper;
    private final String rootPath;

    public BasicServiceDiscovery(PropertyConfigurer configurer){

        String path = configurer.getOrDefault("zookeeper.discovery.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        //服务注册地址
        registryAddress = configurer.getOrDefault("zookeeper.discovery.address", "127.0.0.1:2181");
        LOGGER.debug("Discovering zookeeper service address = " + registryAddress);
        //session timeout in milliseconds
        int sessionTimeout = configurer.getOrDefault("zookeeper.session.timeout", 5000);

        zooKeeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);
    }

    @Override
    public void discover(String serviceName, BiConsumer<String, List<String>> updateMethod) {
        if(zooKeeper != null){
            String absPath = rootPath + "/" + serviceName + "/providers";
            ZooKeeperUtils.watchChildrenData(zooKeeper, absPath, 
                list -> updateMethod.accept(serviceName, list));
        }
    }

    @Override
    public void destroy() throws DestroyFailedException {
        try{
            ZooKeeperUtils.disconnectZooKeeper(zooKeeper);
            LOGGER.debug("Service discovery shuted down.");
        }catch(InterruptedException e){
            throw new DestroyFailedException(e.getMessage());
        }
    }

    @Override
    public boolean isDestroyed() {
        return !zooKeeper.getState().isAlive();
    }

    @Override
    public void register(String serviceName, String data) {
        //创建服务节点
        String serviceNode = rootPath + "/" + serviceName;
        ZooKeeperUtils.createNode(zooKeeper,serviceNode, serviceName);
        //创建消费者节点
        String absPath = rootPath + "/" + serviceName + "/consumers/consumer";
        ZooKeeperUtils.createNode(zooKeeper, absPath, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    
}