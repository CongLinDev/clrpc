package conglin.clrpc.common.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZooKeeperUtils {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperUtils.class);

    /**
     * 同步连接 ZooKeeper
     * 
     * @param address        ZooKeeper 地址
     * @param sessionTimeout 超时时间
     * @return
     */
    public static ZooKeeper connectZooKeeper(String address, int sessionTimeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);

        ZooKeeper keeper = null;
        try {
            keeper = new ZooKeeper(address, sessionTimeout, event -> {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                    log.debug("ZooKeeper address=" + address + " is connected.");
                }
            });
            log.debug("ZooKeeper address=" + address + " is connecting...");
            countDownLatch.await();
        } catch (IOException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return keeper;
    }

    /**
     * 关闭与ZooKeeper的连接
     * 
     * @param keeper
     * @throws InterruptedException
     */
    public static void disconnectZooKeeper(ZooKeeper keeper) throws InterruptedException {
        if (keeper != null) {
            keeper.close();
        }
    }

    /**
     * 递归创建通用的持久节点 该节点不存储任何信息 采用 OPEN_ACL_UNSAFE 策略
     * 
     * @param keeper
     * @param path   绝对路径
     * @param data   存储数据
     * @return 节点路径
     */
    public static String createNode(final ZooKeeper keeper, String path, String data) {
        return createNode(keeper, path, data, CreateMode.PERSISTENT);
    }

    /**
     * 递归创建通用的节点 该节点不存储任何信息 采用 OPEN_ACL_UNSAFE 策略
     * 
     * @param keeper
     * @param path   绝对路径
     * @param mode   节点创建模式
     * @return
     */
    public static String createNode(final ZooKeeper keeper, String path, CreateMode mode) {
        return createNode(keeper, path, "", mode);
    }

    /**
     * 递归创建通用的节点 该节点不存储任何信息 采用 OPEN_ACL_UNSAFE 策略
     * 
     * @param keeper
     * @param path   绝对路径
     * @param data   存储数据
     * @param mode   节点类型
     * @return 节点路径
     */
    public static String createNode(final ZooKeeper keeper, String path, String data, CreateMode mode) {
        // 创建上级节点
        int index = path.lastIndexOf('/');
        createNode(keeper, path.substring(0, index));

        try {
            Stat stat = keeper.exists(path, false);
            if (stat == null) {
                String subPath = keeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
                log.debug("create zookeeper node : " + subPath);
                return subPath;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 递归创建通用的持久节点 该节点不存储任何信息 采用 OPEN_ACL_UNSAFE 策略
     * 
     * @param keeper
     * @param path   绝对路径
     * @return 节点路径
     */
    public static String createNode(final ZooKeeper keeper, String path) {
        if ("".equals(path))
            return null;
        try {
            Stat stat = keeper.exists(path, false);
            if (stat == null) {
                int index = path.lastIndexOf('/');
                String higherLevelPath = path.substring(0, index);
                createNode(keeper, higherLevelPath);
                String subPath = keeper.create(path, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                log.debug("create zookeeper node :" + subPath);
                return subPath;
            }
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 删除节点（其版本总是最新的） 同时递归删除子节点
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static String deleteNode(final ZooKeeper keeper, String path) {
        try {
            List<String> subNodes = keeper.getChildren(path, false);
            for (String subNode : subNodes)
                keeper.delete(path + "/" + subNode, -1);
            keeper.delete(path, -1);
            return path;
        } catch (InterruptedException | KeeperException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 监视节点
     * @param keeper
     * @param path
     * @param watcher
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static String watchNode(final ZooKeeper keeper, String path, Watcher watcher) 
        throws KeeperException, InterruptedException {
            return new String(keeper.getData(path, watcher, null));        
    }

    /**
     * 监视指定路径下所有子节点的数据
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static List<String> watchChildrenData(final ZooKeeper keeper, String path) {
        return watchChildrenData(keeper, path, null);
    }

    /**
     * 监视指定路径下所有子节点的数据
     * 
     * @param keeper
     * @param path
     * @param consumer
     * @return
     */
    public static List<String> watchChildrenData(final ZooKeeper keeper, String path, Consumer<List<String>> consumer) {
        try {
            List<String> nodeList = getChildrenNode(keeper, path, event -> {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    watchChildrenData(keeper, path, consumer);
                }
            });
            List<String> data = getChildrenData(keeper, path, nodeList);
            if (consumer != null)
                consumer.accept(data);
            return data;

        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static Map<String, String> watchChildrenNodeAndData(final ZooKeeper keeper, String path) {
        return watchChildrenNodeAndData(keeper, path, null);
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @param consumer
     * @return
     */
    public static Map<String, String> watchChildrenNodeAndData(final ZooKeeper keeper, String path,
            Consumer<Map<String, String>> consumer) {
        try {
            List<String> nodeList = getChildrenNode(keeper, path, event -> {
                if (event.getType() == Event.EventType.NodeChildrenChanged) {
                    watchChildrenNodeAndData(keeper, path, consumer);
                }
            });
            Map<String, String> nodeAndData = getChildrenNodeAndData(keeper, path, nodeList);
            if (consumer != null)
                consumer.accept(nodeAndData);
            return nodeAndData;
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    /**
     * 获取指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @param nodeList
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static Map<String, String> getChildrenNodeAndData(final ZooKeeper keeper, String path,
            List<String> nodeList) throws KeeperException, InterruptedException {
        Map<String, String> dataMap = new HashMap<>(nodeList.size());

        for (String node : nodeList) {
            String nodePath = path + "/" + node;
            String nodeData = new String(keeper.getData(nodePath, false, null));
            dataMap.put(nodePath, nodeData);
        }
        return dataMap;
    }

    /**
     * 获取指定路径下所有子节点的数据
     * 
     * @param keeper
     * @param path
     * @param nodeList
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static List<String> getChildrenData(final ZooKeeper keeper, String path, List<String> nodeList)
            throws KeeperException, InterruptedException {
        List<String> dataList = new ArrayList<>(nodeList.size());

        for (String node : nodeList) {
            String nodePath = path + "/" + node;
            String nodeData = new String(keeper.getData(nodePath, false, null));
            dataList.add(nodeData);
        }
        return dataList;
    }

    /**
     * 获取并监视子节点
     * 
     * @param keeper
     * @param path
     * @param watcher
     * @return
     */
    public static List<String> getChildrenNode(final ZooKeeper keeper, String path, Watcher watcher) {

        try {
            return keeper.getChildren(path, watcher);
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    /**
     * 设置节点的值
     * 
     * @param keeper
     * @param path
     * @param newData  新的值
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static String setNodeData(final ZooKeeper keeper, String path, String newData)
            throws KeeperException, InterruptedException {
        return setNodeData(keeper, path, newData, -1);
    }

    /**
     * 设置节点的值
     * 
     * @param keeper
     * @param path
     * @param newData  新的值
     * @param version  版本号
     * @return
     * @throws KeeperException
     * @throws InterruptedException
     */
    public static String setNodeData(final ZooKeeper keeper, String path, String newData, int version)
            throws KeeperException, InterruptedException {
        keeper.setData(path, newData.getBytes(), version);
        return newData;
    }

    /**
     * CAS替换节点的值
     * @param keeper
     * @param path
     * @param oldData
     * @param newData
     * @return
     */
    public static boolean compareAndSetNodeData(final ZooKeeper keeper, String path, String oldData, String newData) {
        try {
            Stat currentNodeStat = new Stat();
            String currentData = new String(keeper.getData(path, false, currentNodeStat));
            int currentNodeVersion = currentNodeStat.getVersion();
            return currentData.equals(oldData) &&
                (currentNodeVersion + 1) ==
                    keeper.setData(path, newData.getBytes(), currentNodeVersion).getVersion();
        } catch (KeeperException | InterruptedException e) {
            log.error(e.getMessage());
            return false;
        }
    }
}