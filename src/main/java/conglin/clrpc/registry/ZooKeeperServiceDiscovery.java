package conglin.clrpc.registry;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

/**
 * 注册发现类 默认情况下使用ZooKeeper注册服务 根路径 {root-path} 默认为 /clrpc （可在配置文件中更改）
 * 
 * 例如：对于一个服务 UserService 其路径为 /{root-path}/service/UserService 在该路径下有两个结点
 * /providers 和 /consumers 其子节点分别记录服务提供者的IP和服务消费者的IP
 */
public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private final String rootPath; // zookeeper根地址
    private final ZooKeeper keeper;

    public ZooKeeperServiceDiscovery(PropertyConfigurer configurer) {

        String path = configurer.getOrDefault("zookeeper.discovery.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        // 服务注册地址
        String discoveryAddress = configurer.getOrDefault("zookeeper.discovery.address", "127.0.0.1:2181");
        int sessionTimeout = configurer.getOrDefault("zookeeper.discovery.session-timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(discoveryAddress, sessionTimeout);
        LOGGER.debug("Discovering zookeeper service address = {}", discoveryAddress);
    }

    @Override
    public void discover(String type, BiConsumer<String, Collection<Pair<String, String>>> updater) {
        String providerNodes = rootPath + "/" + type + "/providers";
        ZooKeeperUtils.watchChildrenList(keeper, providerNodes, provider -> updater.accept(type, provider));
    }

    @Override
    public void register(String type, String key, String value) {
        // 创建服务提供者节点
        String consumerNode = rootPath + "/" + type + "/consumers" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.createNode(keeper, consumerNode, value, CreateMode.EPHEMERAL);
        LOGGER.debug("Register a service consumer which consumers {}.", type);
    }

    @Override
    public void unregister(String type, String key) {
        String consumerNode = rootPath + "/" + type + "/consumers" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.deleteNode(keeper, consumerNode);

        LOGGER.debug("Unregister a service consumer which consumers {}.", type);
    }
}