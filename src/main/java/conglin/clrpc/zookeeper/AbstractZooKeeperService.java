package conglin.clrpc.zookeeper;

import org.apache.zookeeper.ZooKeeper;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

abstract public class AbstractZooKeeperService {

    protected final String rootPath; // zookeeper根地址
    protected final ZooKeeper keeper;

    public AbstractZooKeeperService(String role, PropertyConfigurer configurer) {
        this(role, configurer, "service");
    }

    public AbstractZooKeeperService(String role, PropertyConfigurer configurer, String serviceNode) {
        String configPrefix = "zookeeper." + role;
        String path = configurer.getOrDefault(configPrefix + ".root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + serviceNode : path + "/" + serviceNode;

        // 服务注册地址
        String address = configurer.getOrDefault(configPrefix + ".address", "127.0.0.1:2181");
        int sessionTimeout = configurer.getOrDefault(configPrefix + ".session-timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(address, sessionTimeout);
    }
}