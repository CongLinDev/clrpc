package conglin.clrpc.service.registry;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.zookeeper.NodeManager;

/**
 * 注册服务类
 * 默认情况下使用ZooKeeper注册服务
 * 根路径 {root-path} 默认为 /clrpc （可在配置文件中更改）
 * 
 * 例如：对于一个服务 UserService
 * 其路径为 /{root-path}/service/UserService
 * 在该路径下有两个结点 /providers 和 /consumers
 * 其子节点分别记录服务提供者的IP和服务消费者的IP
 */
public class BasicServiceRegistry implements ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(BasicServiceRegistry.class);

    private final String registryAddress; // 服务注册地址

    private final String rootPath; //zookeeper根地址

    public BasicServiceRegistry() {
        // 服务注册地址
        registryAddress = ConfigParser.getInstance().getOrDefault("zookeeper.registry.url", "localhost:2181");
        String path = ConfigParser.getInstance().getOrDefault("zookeeper.registry.root_path", "/clrpc");
        rootPath = path.endsWith("/") ? path.substring(0, path.length()-1) : path;//去除最后一个 /
    }

    /**
     * 注册服务提供者
     * @param serviceName
     * @param data
     */
    @Override
    public void registerProvider(String serviceName, String data){
        ZooKeeper zooKeeper = connectServer();
        if (zooKeeper != null) {
            //创建服务节点
            String serviceNode = rootPath + "/service/" + serviceName;
            NodeManager.createNode(zooKeeper,serviceNode, serviceName);

            //创建服务提供者节点
            String providerNode = rootPath + "/service/" + serviceName + "/providers/provider";
            NodeManager.createNode(zooKeeper, providerNode, data, CreateMode.EPHEMERAL_SEQUENTIAL);
        }
    }

    // public void inregisterProvider(String serviceName, String data){

    // }

    /**
     * 连接Zookeeper服务器
     */
    private ZooKeeper connectServer() {
        // session timeout in milliseconds
        int sessionTimeout = ConfigParser.getInstance().getOrDefault("zookeeper.session.timeout", 5000);
        CountDownLatch countDownLatch = new CountDownLatch(1);

        ZooKeeper keeper = null;
        try {
            keeper = new ZooKeeper(registryAddress, sessionTimeout, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if (event.getState() == Event.KeeperState.SyncConnected) {
                        countDownLatch.countDown();
                        log.debug("ZooKeeper address=" + registryAddress + " connected.");
                    }
                }
            });
            log.debug("ZooKeeper address="+ registryAddress +" connecting...");
            countDownLatch.await();

        } catch (IOException | InterruptedException e) {
            log.error("", e);
        }
        return keeper;
    }
}