package conglin.clrpc.registry;

import java.util.Map;
import java.util.function.BiConsumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

/**
 * 服务发现 使用 Zookeeper 作为服务注册
 */

public class ZooKeeperServiceDiscovery implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    private final String localAddress; // 本地地址

    private final ZooKeeper keeper;
    private final String rootPath;

    public ZooKeeperServiceDiscovery(String localAddress, PropertyConfigurer configurer) {

        String path = configurer.getOrDefault("zookeeper.discovery.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        // 服务注册地址
        String registryAddress = configurer.getOrDefault("zookeeper.discovery.address", "127.0.0.1:2181");
        LOGGER.debug("Discovering zookeeper service address = " + registryAddress);
        int sessionTimeout = configurer.getOrDefault("zookeeper.discovery.session-timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(registryAddress, sessionTimeout);

        this.localAddress = localAddress;
    }

    @Override
    public void discover(String serviceName, BiConsumer<String, Map<String, String>> updateMethod) {
        String providerNodes = rootPath + "/" + serviceName + "/providers";
        ZooKeeperUtils.watchChildrenNodeAndData(keeper, providerNodes, map -> updateMethod.accept(serviceName, map));
    }

    @Override
    public void register(String serviceName, String data) {
        // 创建服务节点
        String serviceNode = rootPath + "/" + serviceName;
        ZooKeeperUtils.createNode(keeper, serviceNode, serviceName);
        // 创建消费者节点
        String consumerNode = rootPath + "/" + serviceName + "/consumers/" + localAddress;
        ZooKeeperUtils.createNode(keeper, consumerNode, data, CreateMode.EPHEMERAL);

        LOGGER.debug("Register a service consumer which consumers " + serviceName);
    }

    @Override
    public void unregister(String serviceName) {
        // 移除服务消费者节点
        String consumerNode = rootPath + "/" + serviceName + "/consumers/" + localAddress;
        ZooKeeperUtils.deleteNode(keeper, consumerNode);

        LOGGER.debug("Unregister a service consumer which consumers " + serviceName);
    }
}