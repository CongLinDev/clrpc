package conglin.clrpc.zookeeper.registry;

import java.util.Collection;
import java.util.function.BiConsumer;

import org.apache.zookeeper.Watcher.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.registry.ServiceMonitor;
import conglin.clrpc.zookeeper.AbstractZooKeeperService;
import conglin.clrpc.zookeeper.util.ZooKeeperUtils;

public class ZooKeeperServiceMonitor extends AbstractZooKeeperService implements ServiceMonitor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperServiceMonitor.class);

    public ZooKeeperServiceMonitor(PropertyConfigurer configurer) {
        super("monitor", configurer);        
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