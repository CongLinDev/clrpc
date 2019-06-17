package conglin.clrpc.service.registry;

import java.io.IOException;
import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;

public class BasicServiceRegistry implements ServiceRegistry {

    private static final Logger log = LoggerFactory.getLogger(BasicServiceRegistry.class);

    private final String registryAddress; // 服务注册地址

    public BasicServiceRegistry() {
        // 服务注册地址
        registryAddress = ConfigParser.getInstance().getOrDefault("zookeeper.registry.url", "localhost:2181");
    }

    @Override
    public void register(String data) {
        if (data != null) {
            ZooKeeper zooKeeper = connectServer();
            if (zooKeeper != null) {
                createRootNode(zooKeeper);
                createNode(zooKeeper, data);
            }
        }
    }

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
                    }
                }
            });
            countDownLatch.await();

        } catch (IOException | InterruptedException e) {
            log.error("", e);
        }
        return keeper;
    }

    /**
     * 如果没有根节点，则创建根节点 
     * 反之没有任何动作
     * @param keeper
     */
    private void createRootNode(ZooKeeper keeper) {
        String path = ConfigParser.getInstance().getOrDefault("zookeeper.registry.root_path", "/");
        try {
            Stat stat = keeper.exists(path, false);
            if (stat == null) {
                keeper.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("", e);
        }
    }

    /**
     * 创建节点
     * @param keeper
     * @param data
     */
    private void createNode(ZooKeeper keeper, String data) {
        byte[] bytes = data.getBytes();
        String path = ConfigParser.getInstance().getOrDefault("zookeeper.registry.root_path", "/");

        try {
            String subPath = keeper.create(path, bytes, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
            log.debug("create zookeeper node ({} => {})", subPath, data);
        } catch (KeeperException | InterruptedException e) {
            log.error("", e);
        }
    }
}