package conglin.clrpc.bootstrap.monitor;

import java.util.List;
import java.util.Map;

import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.Watcher.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.bootstrap.monitor.RpcMonitorBootstrap;
import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.common.util.ZooKeeperUtils;

abstract public class AbstractRpcMonitorBootstrap implements RpcMonitorBootstrap {

    private static final Logger log = LoggerFactory.getLogger(AbstractRpcMonitorBootstrap.class);

    protected ZooKeeper zooKeeper;
    protected String rootPath;
    private final int sessionTimeout;

    public AbstractRpcMonitorBootstrap() {
        this.sessionTimeout = ConfigParser.getOrDefault("zookeeper.session.timeout", 5000);
    }

    public AbstractRpcMonitorBootstrap(int sessionTimeout) {
        this.sessionTimeout = sessionTimeout;
    }

    @Override
    public RpcMonitorBootstrap monitor(){
        String monitorAddress = ConfigParser.getOrDefault("zookeeper.monitor.address", "localhost:2181");
        String path = ConfigParser.getOrDefault("zookeeper.monitor.root-path", "/clrpc");
        return monitor(monitorAddress, path);
    }

    @Override
    public RpcMonitorBootstrap monitor(String zooKeeperAddress, String path) {
        // 连接ZooKeeper
        this.rootPath = path.endsWith("/") ? path + "service" : path + "/service";
        zooKeeper = ZooKeeperUtils.connectZooKeeper(zooKeeperAddress, sessionTimeout);
        log.info("Starting to monitor zookeeper whose address="+ zooKeeperAddress + "  root-path=" + path);
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
        
        log.info("Monitor service named " + serviceName);
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