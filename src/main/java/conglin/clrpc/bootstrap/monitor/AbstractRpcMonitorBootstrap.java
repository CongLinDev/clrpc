package conglin.clrpc.bootstrap.monitor;

import java.util.List;
import java.util.Map;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.RpcBootstrap;
import conglin.clrpc.bootstrap.RpcMonitorBootstrap;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

abstract public class AbstractRpcMonitorBootstrap extends RpcBootstrap implements RpcMonitorBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcMonitorBootstrap.class);

    protected final ZooKeeper keeper;
    protected final String rootPath;

    public AbstractRpcMonitorBootstrap() {
        this(null);
    }

    public AbstractRpcMonitorBootstrap(PropertyConfigurer configurer) {
        super(configurer);
        int sessionTimeout = CONFIGURER.getOrDefault("zookeeper.monitor.session-timeout", 5000);

        String monitorAddress = CONFIGURER.getOrDefault("zookeeper.monitor.address", "127.0.0.1:2181");
        String path = CONFIGURER.getOrDefault("zookeeper.monitor.root-path", "/clrpc");
        rootPath = path.endsWith("/") ? path + "service" : path + "/service";
        keeper = ZooKeeperUtils.connectZooKeeper(monitorAddress, sessionTimeout);
        LOGGER.info("Starting to monitor zookeeper whose address=" + monitorAddress + "  root-path=" + rootPath);
    }

    @Override
    public RpcMonitorBootstrap monitor() {
        List<String> services = ZooKeeperUtils.getChildrenNode(keeper, rootPath, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                monitor();
            }
        });
        services.forEach(this::monitor);
        return this;
    }

    @Override
    public RpcMonitorBootstrap monitor(String serviceName) {
        String concretePath = rootPath + "/" + serviceName;
        String providerPath = concretePath + "/providers";
        String consumerPath = concretePath + "/consumers";

        LOGGER.info("Monitor service named " + serviceName);
        ZooKeeperUtils.watchChildrenNodeAndData(keeper, providerPath, serviceName, this::handleProvider);
        ZooKeeperUtils.watchChildrenNodeAndData(keeper, consumerPath, serviceName, this::handleConusmer);
        return this;
    }

    @Override
    public void start() {
        super.start();
    }

    @Override
    public void stop() {
        super.stop();
    }

    /**
     * 处理服务消费者节点和数据
     * 
     * @param serviceName
     * @param nodeAndData
     */
    abstract protected void handleConusmer(String serviceName, Map<String, String> nodeAndData);

    /**
     * 处理服务提供者节点和数据
     * 
     * @param serviceName
     * @param nodeAndData
     */
    abstract protected void handleProvider(String serviceName, Map<String, String> nodeAndData);
}