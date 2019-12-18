package conglin.clrpc.common.util.atomic;

import org.apache.zookeeper.ZooKeeper;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

abstract public class ZooKeeperAtomicService {
    protected final ZooKeeper keeper;
    protected final String rootPath;

    public ZooKeeperAtomicService(PropertyConfigurer configurer) {
        this(configurer, "/unnamed");
    }

    public ZooKeeperAtomicService(PropertyConfigurer configurer, String subPath) {
        this(configurer.getOrDefault("zookeeper.atomicity.address", "127.0.0.1:2181"),
                configurer.getOrDefault("zookeeper.atomicity.session-timeout", 5000),
                configurer.getOrDefault("zookeeper.atomicity.root-path", "/clrpc"), subPath);
    }

    public ZooKeeperAtomicService(String atomicityAddress, int sessionTimeout, String mainPath, String subPath) {
        rootPath = mainPath.endsWith("/") ? mainPath + "atomic" + subPath : mainPath + "/atomic" + subPath;
        keeper = ZooKeeperUtils.connectZooKeeper(atomicityAddress, sessionTimeout);
    }
}