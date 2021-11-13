package conglin.clrpc.thirdparty.zookeeper.registry;

import conglin.clrpc.common.object.Pair;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.registry.ServiceDiscovery;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.thirdparty.zookeeper.util.ZooKeeperUtils;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.function.BiConsumer;

/**
 * 注册发现类 默认情况下使用ZooKeeper注册服务 根路径 {root-path} 默认为 /clrpc （可在配置文件中更改）
 * 
 * 例如：对于一个服务 UserService 其路径为 /{root-path}/service/UserService 在该路径下有两个结点
 * /provider 和 /consumer 其子节点分别记录服务提供者的IP和服务消费者的IP
 */
public class ZooKeeperServiceDiscovery extends AbstractZooKeeperService implements ServiceDiscovery {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceDiscovery.class);

    public ZooKeeperServiceDiscovery(UrlScheme url) {
        super(url, "service");
    }

    @Override
    public void publish(String type, String value) {
        String path = rootPath + "/" + type;
        if (ZooKeeperUtils.isNotExistNode(keeperInstance.instance(), path)) {
            ZooKeeperUtils.createNode(keeperInstance.instance(), path, value);
        }
    }

    @Override
    public void discover(String type, BiConsumer<String, Collection<Pair<String, String>>> updater) {
        String providerNodes = rootPath + "/" + type + "/provider";
        ZooKeeperUtils.watchChildrenList(keeperInstance.instance(), providerNodes, values -> updater.accept(type, values));
    }

    @Override
    public void register(String type, String key, String value) {
        // 创建服务提供者节点
        String consumerNode = rootPath + "/" + type + "/consumer" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.createNode(keeperInstance.instance(), consumerNode, value, CreateMode.EPHEMERAL);
        LOGGER.debug("Register a service consumer which consumers {}.", type);
    }

    @Override
    public void unregister(String type, String key) {
        String consumerNode = rootPath + "/" + type + "/consumer" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.deleteNode(keeperInstance.instance(), consumerNode);

        LOGGER.debug("Unregister a service consumer which consumers {}.", type);
    }
}