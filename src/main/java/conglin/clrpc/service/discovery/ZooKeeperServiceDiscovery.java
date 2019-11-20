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

public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);
    
    private final ZooKeeper keeper;
    private final String rootPath;

    public ZooKeeperServiceDiscovery(PropertyConfigurer configurer){

        String path = configurer.getOrDefault("zookeeper.discovery.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        //服务注册地址
        String registryAddress = configurer.getOrDefault("zookeeper.discovery.address", "127.0.0.1:2181");
        LOGGER.debug("Discovering zookeeper service address = " + registryAddress);
        //session timeout in milliseconds
        int sessionTimeout = configurer.getOrDefault("zookeeper.session.timeout", 5000);

        keeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);
    }

    @Override
    public void discover(String serviceName, BiConsumer<String, List<String>> updateMethod) {
        if(keeper != null){
            String absPath = rootPath + "/" + serviceName + "/providers";
            ZooKeeperUtils.watchChildrenData(keeper, absPath, 
                list -> updateMethod.accept(serviceName, list));
        }
    }

    @Override
    public void destroy() throws DestroyFailedException {
        try{
            ZooKeeperUtils.disconnectZooKeeper(keeper);
            LOGGER.debug("Service discovery shuted down.");
        }catch(InterruptedException e){
            throw new DestroyFailedException(e.getMessage());
        }
    }

    @Override
    public boolean isDestroyed() {
        return !keeper.getState().isAlive();
    }

    @Override
    public void register(String serviceName, String data) {
        //创建服务节点
        String serviceNode = rootPath + "/" + serviceName;
        ZooKeeperUtils.createNode(keeper,serviceNode, serviceName);
        //创建消费者节点
        String absPath = rootPath + "/" + serviceName + "/consumers/consumer";
        ZooKeeperUtils.createNode(keeper, absPath, data, CreateMode.EPHEMERAL_SEQUENTIAL);
    }
    
}