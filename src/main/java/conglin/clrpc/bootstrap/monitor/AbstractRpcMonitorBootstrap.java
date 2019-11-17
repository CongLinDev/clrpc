package conglin.clrpc.bootstrap.monitor;

import java.util.List;
import java.util.Map;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.Bootstrap;
import conglin.clrpc.bootstrap.RpcMonitorBootstrap;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.config.YamlPropertyConfigurer;
import conglin.clrpc.common.util.ZooKeeperUtils;

abstract public class AbstractRpcMonitorBootstrap extends Bootstrap implements RpcMonitorBootstrap {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRpcMonitorBootstrap.class);

    protected ZooKeeper zooKeeper;
    protected String rootPath;
    private final int SESSION_TIMEOUT;

    public AbstractRpcMonitorBootstrap() {
        this(new YamlPropertyConfigurer());
    }

    public AbstractRpcMonitorBootstrap(PropertyConfigurer configurer) {
        super(configurer, false);
        this.SESSION_TIMEOUT = configurer.getOrDefault("zookeeper.session.timeout", 5000);
    }

    @Override
    public RpcMonitorBootstrap monitor(){
        String monitorAddress = CONFIGURER.getOrDefault("zookeeper.monitor.address", "127.0.0.1:2181");
        String path = CONFIGURER.getOrDefault("zookeeper.monitor.root-path", "/clrpc");
        return monitor(monitorAddress, path);
    }

    @Override
    public RpcMonitorBootstrap monitor(String zooKeeperAddress, String path) {
        // 连接ZooKeeper
        this.rootPath = path.endsWith("/") ? path + "service" : path + "/service";
        zooKeeper = ZooKeeperUtils.connectZooKeeper(zooKeeperAddress, SESSION_TIMEOUT);
        LOGGER.info("Starting to monitor zookeeper whose address="+ zooKeeperAddress + "  root-path=" + path);
        return this;
    }
    
    @Override
    public RpcMonitorBootstrap monitorService() {
        List<String> services = ZooKeeperUtils.getChildrenNode(zooKeeper, rootPath, event -> {
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
        ZooKeeperUtils.watchChildrenNodeAndData(zooKeeper, providerPath, this::handleNodeInfo);
        ZooKeeperUtils.watchChildrenNodeAndData(zooKeeper, consumerPath, this::handleNodeInfo);
        return this;
    }

    @Override
    public void stop() throws InterruptedException {
        ZooKeeperUtils.disconnectZooKeeper(zooKeeper);
    }

    /**
     * 处理节点和数据
     * 该方法由子类实现
     * @param nodeAndData
     */
    abstract protected void handleNodeInfo(Map<String, String> nodeAndData);
}