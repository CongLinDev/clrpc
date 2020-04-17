package conglin.clrpc.zookeeper.registry;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.registry.ServiceDiscovery;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

/**
 * 注册发现类 默认情况下使用ZooKeeper注册服务 根路径 {root-path} 默认为 /clrpc （可在配置文件中更改）
 * 
 * 例如：对于一个服务 UserService 其路径为 /{root-path}/service/UserService 在该路径下有两个结点
 * /provider 和 /consumer 其子节点分别记录服务提供者的IP和服务消费者的IP
 */
public class ZooKeeperServiceDiscovery extends AbstractZooKeeperService implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    public ZooKeeperServiceDiscovery(PropertyConfigurer configurer) {
        super(Role.CONSUMER, configurer);
    }

    @Override
    public void publish(String type, String value) {
        String path = rootPath + "/" + type;
        if(!ZooKeeperUtils.isExistNode(keeper, path)) {
            ZooKeeperUtils.createNode(keeper, path, value);
        }
    }

    @Override
    public void discover(String type, BiConsumer<String, Collection<Pair<String, String>>> updater) {
        String providerNodes = rootPath + "/" + type + "/provider";
        ZooKeeperUtils.watchChildrenList(keeper, providerNodes, provider -> updater.accept(type, provider));
    }

    @Override
    public void register(String type, String key, String value) {
        // 创建服务提供者节点
        String consumerNode = rootPath + "/" + type + "/consumer" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.createNode(keeper, consumerNode, value, CreateMode.EPHEMERAL);
        LOGGER.debug("Register a service consumer which consumers {}.", type);
    }

    @Override
    public void unregister(String type, String key) {
        String consumerNode = rootPath + "/" + type + "/consumer" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.deleteNode(keeper, consumerNode);

        LOGGER.debug("Unregister a service consumer which consumers {}.", type);
    }
}