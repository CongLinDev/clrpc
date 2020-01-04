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

    protected ZooKeeper keeper;
    protected String rootPath;
    private final int SESSION_TIMEOUT;

    public AbstractRpcMonitorBootstrap() {
        super(false);
        this.SESSION_TIMEOUT = CONFIGURER.getOrDefault("zookeeper.monitor.session-timeout", 5000);
    }

    public AbstractRpcMonitorBootstrap(String configFilename) {
        super(configFilename, false);
        this.SESSION_TIMEOUT = CONFIGURER.getOrDefault("zookeeper.monitor.session-timeout", 5000);
    }

    public AbstractRpcMonitorBootstrap(PropertyConfigurer configurer) {
        super(configurer, false);
        this.SESSION_TIMEOUT = CONFIGURER.getOrDefault("zookeeper.monitor.session-timeout", 5000);
    }

    @Override
    public RpcMonitorBootstrap monitor() {
        String monitorAddress = CONFIGURER.getOrDefault("zookeeper.monitor.address", "127.0.0.1:2181");
        String path = CONFIGURER.getOrDefault("zookeeper.monitor.root-path", "/clrpc");
        return monitor(monitorAddress, path);
    }

    @Override
    public RpcMonitorBootstrap monitor(String zooKeeperAddress, String path) {
        // 连接ZooKeeper
        this.rootPath = path.endsWith("/") ? path + "service" : path + "/service";
        keeper = ZooKeeperUtils.connectZooKeeper(zooKeeperAddress, SESSION_TIMEOUT);
        LOGGER.info("Starting to monitor zookeeper whose address=" + zooKeeperAddress + "  root-path=" + path);
        return this;
    }

    @Override
    public RpcMonitorBootstrap monitorService() {
        List<String> services = ZooKeeperUtils.getChildrenNode(keeper, rootPath, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                monitorService();
            }
        });
        services.forEach(this::monitorService);
        return this;
    }

    @Override
    public RpcMonitorBootstrap monitorService(String serviceName) {
        String concretePath = rootPath + "/" + serviceName;
        String providerPath = concretePath + "/providers";
        String consumerPath = concretePath + "/consumers";

        LOGGER.info("Monitor service named " + serviceName);
        ZooKeeperUtils.watchChildrenNodeAndData(keeper, providerPath, this::handleProvider);
        ZooKeeperUtils.watchChildrenNodeAndData(keeper, consumerPath, this::handleConusmer);
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
     * @param nodeAndData
     */
    abstract protected void handleConusmer(Map<String, String> nodeAndData);

    /**
     * 处理服务提供者节点和数据
     * 
     * @param nodeAndData
     */
    abstract protected void handleProvider(Map<String, String> nodeAndData);
}