package conglin.clrpc.zookeeper.registry;

import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Url;
import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

/**
 * 注册服务类 默认情况下使用ZooKeeper注册服务 根路径 {root-path} 默认为 /clrpc （可在配置文件中更改）
 * 
 * 例如：对于一个服务 UserService 其路径为 /{root-path}/service/UserService 在该路径下有两个结点
 * /provider 和 /consumer 其子节点分别记录服务提供者的IP和服务消费者的IP
 */
public class ZooKeeperServiceRegistry extends AbstractZooKeeperService implements ServiceRegistry {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceRegistry.class);

    public ZooKeeperServiceRegistry(Url url) {
        super(url, "service");
    }

    @Override
    public void publish(String type, String value) {
        String path = rootPath + "/" + type;
        if(!ZooKeeperUtils.isExistNode(keeper, path)) {
            ZooKeeperUtils.createNode(keeper, path, value);
        }
    }

    @Override
    public void register(String type, String key, String value) {
        // 创建服务提供者节点
        String providerNode = rootPath + "/" + type + "/provider" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.createNode(keeper, providerNode, value, CreateMode.EPHEMERAL);
    }

    @Override
    public void unregister(String type, String key) {
        String providerNode = rootPath + "/" + type + "/provider" + (key.startsWith("/") ? key : "/" + key);
        ZooKeeperUtils.deleteNode(keeper, providerNode);
        LOGGER.debug("Unregister a service provider which provides {}.", type);
    }
}