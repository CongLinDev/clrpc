package conglin.clrpc.service.registry;

import javax.security.auth.DestroyFailedException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

/**
 * 注册服务类
 * 默认情况下使用ZooKeeper注册服务
 * 根路径 {root-path} 默认为 /clrpc （可在配置文件中更改）
 * 
 * 例如：对于一个服务 UserService
 * 其路径为 /{root-path}/service/UserService
 * 在该路径下有两个结点 /providers 和 /consumers
 * 其子节点分别记录服务提供者的IP和服务消费者的IP
 */
public class BasicServiceRegistry implements ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(BasicServiceRegistry.class);

    private final String rootPath; //zookeeper根地址
    private final ZooKeeper zooKeeper;

    public BasicServiceRegistry(PropertyConfigurer configurer) {

        String path = configurer.getOrDefault("zookeeper.registry.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        // 服务注册地址
        String registryAddress = configurer.getOrDefault("zookeeper.registry.address", "127.0.0.1:2181");
        int sessionTimeout = configurer.getOrDefault("zookeeper.session.timeout", 5000);
        zooKeeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);
    }

    /**
     * 注册服务提供者
     * @param serviceName
     * @param data
     */
    @Override
    public void registerProvider(String serviceName, String data){
        if (zooKeeper != null) {
            //创建服务节点
            String serviceNode = rootPath + "/" + serviceName;
            ZooKeeperUtils.createNode(zooKeeper,serviceNode, serviceName);

            //创建服务提供者节点
            String providerNode = rootPath + "/" + serviceName + "/providers/provider";
            ZooKeeperUtils.createNode(zooKeeper, providerNode, data, CreateMode.EPHEMERAL_SEQUENTIAL);

            log.debug("Create a service provider which provides " + serviceName);
        }
    }

    @Override
    public void destroy() throws DestroyFailedException {
        try{
            ZooKeeperUtils.disconnectZooKeeper(zooKeeper);
            log.debug("Service registry shuted down.");
        }catch(InterruptedException e){
            log.error(e.getMessage());
            throw new DestroyFailedException(e.getMessage());
        }
    }

    @Override
    public boolean isDestroyed() {
        return !zooKeeper.getState().isAlive();
    }
}