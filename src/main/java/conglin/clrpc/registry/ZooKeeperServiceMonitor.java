package conglin.clrpc.registry;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

public class ZooKeeperServiceMonitor implements ServiceMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceMonitor.class);

    private final ZooKeeper keeper;
    private final String rootPath;

    public ZooKeeperServiceMonitor(PropertyConfigurer configurer) {

        String path = configurer.getOrDefault("zookeeper.monitor.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";

        // 服务注册地址
        String monitorAddress = configurer.getOrDefault("zookeeper.monitor.address", "127.0.0.1:2181");
        int sessionTimeout = configurer.getOrDefault("zookeeper.monitor.session-timeout", 5000);
        keeper = ZooKeeperUtils.connectZooKeeper(monitorAddress, sessionTimeout);
        LOGGER.debug("Monitoring zookeeper service address = {}", monitorAddress);
    }

    @Override
    public Collection<String> listServices() {
        return ZooKeeperUtils.listChildrenNode(keeper, rootPath, null);
    }

    @Override
    public void monitor(BiConsumer<String, Collection<Pair<String, String>>> handleProvider,
            BiConsumer<String, Collection<Pair<String, String>>> handleConsumer) {
        Collection<String> services = ZooKeeperUtils.listChildrenNode(keeper, rootPath, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                monitor(handleProvider, handleConsumer);
            }
        });

        services.forEach(service -> monitor(service, handleProvider, handleConsumer));
    }

    @Override
    public void monitor(String serviceName, BiConsumer<String, Collection<Pair<String, String>>> handleProvider,
            BiConsumer<String, Collection<Pair<String, String>>> handleConsumer) {
        String concretePath = rootPath + "/" + serviceName;
        String providerPath = concretePath + "/providers";
        String consumerPath = concretePath + "/consumers";
        LOGGER.info("Monitor service named {}.", serviceName);
        ZooKeeperUtils.watchChildrenList(keeper, providerPath, serviceName, handleProvider);
        ZooKeeperUtils.watchChildrenList(keeper, consumerPath, serviceName, handleConsumer);
    }
}