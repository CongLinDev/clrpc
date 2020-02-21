package conglin.clrpc.registry;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

/**
 * 注册服务类 默认情况下使用ZooKeeper注册服务 根路径 {root-path} 默认为 /clrpc （可在配置文件中更改）
 * 
 * 例如：对于一个服务 UserService 其路径为 /{root-path}/service/UserService 在该路径下有两个结点
 * /providers 和 /consumers 其子节点分别记录服务提供者的IP和服务消费者的IP
 */
public class ZooKeeperServiceRegistry implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    private final String rootPath; // zookeeper根地址
    private final ZooKeeper keeper;

    public ZooKeeperServiceRegistry(PropertyConfigurer configurer) {
        String path = configurer.getOrDefault("zookeeper.registry.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        // 服务注册地址
        String address = configurer.getOrDefault("zookeeper.registry.address", "127.0.0.1:2181");
        int sessionTimeout = configurer.getOrDefault("zookeeper.registry.session-timeout", 5000);
        LOGGER.debug("Registering zookeeper service address = {}", address);
        keeper = ZooKeeperUtils.connectZooKeeper(address, sessionTimeout);
    }

    @Override
    public void register(String type, String key, String value) {
        // 创建服务提供者节点
        String providerNode = rootPath + "/" + type + "/providers" + (key.charAt(0) == '/' ? key : "/" + key);
        ZooKeeperUtils.createNode(keeper, providerNode, value, CreateMode.EPHEMERAL);
    }

    @Override
    public void unregister(String type, String key) {
        String providerNode = rootPath + "/" + type + "/providers" + (key.charAt(0) == '/' ? key : "/" + key);
        ZooKeeperUtils.deleteNode(keeper, providerNode);
        LOGGER.debug("Unregister a service provider which provides {}.", type);
    }
}