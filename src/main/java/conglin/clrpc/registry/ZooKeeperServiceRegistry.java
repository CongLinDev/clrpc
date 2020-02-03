package conglin.clrpc.registry;

import java.net.InetSocketAddress;

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

    private final String localAddress; // 本地地址

    private final String rootPath; // zookeeper根地址
    private final ZooKeeper keeper;

    public ZooKeeperServiceRegistry(InetSocketAddress localAddress, PropertyConfigurer configurer) {
        this(localAddress.toString(), configurer);
    }

    public ZooKeeperServiceRegistry(String localAddress, PropertyConfigurer configurer) {

        String path = configurer.getOrDefault("zookeeper.registry.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        // 服务注册地址
        String registryAddress = configurer.getOrDefault("zookeeper.registry.address", "127.0.0.1:2181");
        int sessionTimeout = configurer.getOrDefault("zookeeper.registry.session-timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);
        LOGGER.debug("Registering zookeeper service address = {}", registryAddress);

        this.localAddress = localAddress.charAt(0) == '/' ? localAddress : "/" + localAddress;
    }

    @Override
    public void register(String serviceName, String data) {
        // 创建服务节点
        String serviceNode = rootPath + "/" + serviceName;
        ZooKeeperUtils.createNode(keeper, serviceNode, serviceName);

        // 创建服务提供者节点
        String providerNode = rootPath + "/" + serviceName + "/providers" + localAddress;
        ZooKeeperUtils.createNode(keeper, providerNode, data, CreateMode.EPHEMERAL);

        LOGGER.debug("Register a service provider which provides {}.", serviceName);
    }

    @Override
    public void unregister(String serviceName) {
        // 移除服务提供者节点
        String providerNode = rootPath + "/" + serviceName + "/providers" + localAddress;
        ZooKeeperUtils.deleteNode(keeper, providerNode);

        LOGGER.debug("Unregister a service provider which provides {}.", serviceName);

    }
}