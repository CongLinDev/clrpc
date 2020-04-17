package conglin.clrpc.zookeeper.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.WatcherType;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;

/**
 * ZooKeeper 工具类
 */
public final class ZooKeeperUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZooKeeperUtils.class);

    private static final Map<String, ZooKeeper> ZOOKEEPER_CONNECTION_POOL = new HashMap<>();

    private static final int DEFAULT_SESSION_TIMEOUT = 5000; // ms

    private ZooKeeperUtils() {
        // Unused.
    }

    /**
     * 复用ZooKeeper连接
     * 
     * @param address
     * @return
     */
    public static ZooKeeper connectZooKeeper(final String address) {
        return connectZooKeeper(address, DEFAULT_SESSION_TIMEOUT);
    }

    /**
     * 复用ZooKeeper连接
     * 
     * @param address
     * @param sessionTimeout
     * @return
     */
    public static ZooKeeper connectZooKeeper(final String address, final int sessionTimeout) {
        String key = address + " " + sessionTimeout;
        return ZOOKEEPER_CONNECTION_POOL.computeIfAbsent(key, string -> connectNewZooKeeper(address, sessionTimeout));
    }

    /**
     * 同步连接 ZooKeeper
     * 
     * @param address        ZooKeeper 地址
     * @param sessionTimeout 超时时间
     * @return
     */
    public static ZooKeeper connectNewZooKeeper(final String address, final int sessionTimeout) {
        CountDownLatch countDownLatch = new CountDownLatch(1);
        try {
            ZooKeeper keeper = new ZooKeeper(address, sessionTimeout, event -> {
                if (event.getState() == Event.KeeperState.SyncConnected) {
                    countDownLatch.countDown();
                    LOGGER.debug("ZooKeeper address={} is connected.", address);
                }
            });
            LOGGER.debug("ZooKeeper address={} is connecting...", address);
            countDownLatch.await();
            return keeper;
        } catch (IOException | InterruptedException e) {
            LOGGER.error("ZooKeeper address={} connected failed. Cause: {}", address, e.getMessage());
            if (countDownLatch.getCount() == 1) // count down
                countDownLatch.countDown();
        }
        return null;
    }

    /**
     * 关闭与ZooKeeper的连接
     * 
     * @param keeper
     */
    public static void disconnectZooKeeper(final ZooKeeper keeper) {
        if (keeper != null && keeper.getState().isAlive()) {
            try {
                keeper.close();
                LOGGER.debug("ZooKeeper session(id={}) close success.", keeper.getSessionId());
            } catch (InterruptedException e) {
                LOGGER.error("ZooKeeper session close failed. Cause: {}", e.getMessage());
            }
        }
    }

    /**
     * 关闭 ZooKeeper 连接池
     */
    public static void disconnectAllZooKeeper() {
        ZOOKEEPER_CONNECTION_POOL.values().forEach(ZooKeeperUtils::disconnectZooKeeper);
    }

    /**
     * 是否存在Node
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static boolean isExistNode(final ZooKeeper keeper, String path) {
        try {
            return keeper.exists(path, false) != null;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.getMessage());
        }
        return false;
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
        createNode(keeper, path.substring(0, path.lastIndexOf('/')));

        try {
            Stat stat = keeper.exists(path, false);
            if (stat == null) {
                String subPath = keeper.create(path, data.getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
                LOGGER.debug("Zookeeper create {} node whose path is {}", mode, subPath);
                return subPath;
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.getMessage());
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
                LOGGER.debug("Zookeeper create PERSISTENT node whose path is {}", subPath);
                return subPath;
            }
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.getMessage());
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
            // 递归删除子节点
            listChildrenNode(keeper, path, null).forEach(sub -> deleteNode(keeper, path + "/" + sub));
            // 删除当前节点
            keeper.delete(path, -1);
            return path;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("ZooKeeper delete node failed. Cause: {}.", e.getMessage());
        }
        return null;
    }

    /**
     * 监视节点
     * 
     * @param keeper
     * @param path
     * @param watcher
     * @return 节点的值
     */
    public static String watchNode(final ZooKeeper keeper, String path, Watcher watcher) {
        try {
            return new String(keeper.getData(path, watcher, null));
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("ZooKeeper watch node failed. Cause: {}", e.getMessage());
        }
        return null;
    }

    /**
     * 监视指定路径下所有子节点的数据
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static Collection<String> watchChildrenData(final ZooKeeper keeper, String path) {
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
    public static Collection<String> watchChildrenData(final ZooKeeper keeper, String path,
            Consumer<Collection<String>> consumer) {
        Collection<String> nodeList = listChildrenNode(keeper, path, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                watchChildrenData(keeper, path, consumer);
            }
        });
        Collection<String> data = listChildrenData(keeper, path, nodeList);
        if (consumer != null)
            consumer.accept(data);
        return data;
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static Collection<Pair<String, String>> watchChildrenList(final ZooKeeper keeper, String path) {
        return watchChildrenList(keeper, path, null);
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @param consumer
     * @return
     */
    public static Collection<Pair<String, String>> watchChildrenList(final ZooKeeper keeper, String path,
            Consumer<Collection<Pair<String, String>>> consumer) {
        Collection<String> nodeList = listChildrenNode(keeper, path, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                watchChildrenList(keeper, path, consumer);
            }
        });
        Collection<Pair<String, String>> nodeAndData = listChildren(keeper, path, nodeList);
        if (consumer != null)
            consumer.accept(nodeAndData);
        return nodeAndData;
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @param group    组的标识符
     * @param consumer
     * @return
     */
    public static Collection<Pair<String, String>> watchChildrenList(final ZooKeeper keeper, String path, String group,
            BiConsumer<String, Collection<Pair<String, String>>> consumer) {
        Collection<String> nodeList = listChildrenNode(keeper, path, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                watchChildrenList(keeper, path, group, consumer);
            }
        });
        Collection<Pair<String, String>> nodeAndData = listChildren(keeper, path, nodeList);
        if (consumer != null)
            consumer.accept(group, nodeAndData);
        return nodeAndData;
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static Map<String, String> watchChildrenMap(final ZooKeeper keeper, String path) {
        return watchChildrenMap(keeper, path, null);
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @param consumer
     * @return
     */
    public static Map<String, String> watchChildrenMap(final ZooKeeper keeper, String path,
            Consumer<Map<String, String>> consumer) {
        Collection<String> nodeList = listChildrenNode(keeper, path, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                watchChildrenMap(keeper, path, consumer);
            }
        });
        Map<String, String> nodeAndData = mapChildren(keeper, path, nodeList);
        if (consumer != null)
            consumer.accept(nodeAndData);
        return nodeAndData;
    }

    /**
     * 监视指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @param group    组的标识符
     * @param consumer
     * @return
     */
    public static Map<String, String> watchChildrenMap(final ZooKeeper keeper, String path, String group,
            BiConsumer<String, Map<String, String>> consumer) {
        Collection<String> nodeList = listChildrenNode(keeper, path, event -> {
            if (event.getType() == Event.EventType.NodeChildrenChanged) {
                watchChildrenMap(keeper, path, group, consumer);
            }
        });
        Map<String, String> nodeAndData = mapChildren(keeper, path, nodeList);
        if (consumer != null)
            consumer.accept(group, nodeAndData);
        return nodeAndData;
    }

    /**
     * 移除给定路径下的给定类型的指定Watcher
     * 
     * @param keeper
     * @param path
     * @param watcher     移除的watcher
     * @param watcherType 移除的watcher类型
     */
    public static void removeWatcher(final ZooKeeper keeper, String path, Watcher watcher, WatcherType watcherType) {
        try {
            keeper.removeWatches(path, watcher, watcherType, true);
            LOGGER.debug("ZooKeeper remove watcher of path={}", path);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("ZooKeeper remove watcher of path={} failed. Cause: {}", path, e);
        }
    }

    /**
     * 移除给定路径下的所有类型的Watcher
     * 
     * @param keeper
     * @param path
     */
    public static void removeAllWatchers(final ZooKeeper keeper, String path) {
        removeAllWatchers(keeper, path, WatcherType.Any);
    }

    /**
     * 移除给定路径下的指定类型的Watcher
     * 
     * @param keeper
     * @param path
     * @param watcherType 移除的watcher类型
     */
    public static void removeAllWatchers(final ZooKeeper keeper, String path, WatcherType watcherType) {
        try {
            keeper.removeAllWatches(path, watcherType, true);
            LOGGER.debug("ZooKeeper remove all watchers of path={}", path);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("ZooKeeper remove all watchers of path={} failed. Cause: {}", path, e);
        }
    }

    /**
     * 获取指定路径下所有子节点的名称和数据
     * 
     * @param keeper
     * @param path
     * @param nodeList
     * @return
     */
    public static Map<String, String> mapChildren(final ZooKeeper keeper, String path, Collection<String> nodeList) {
        int size = nodeList.size();
        if (size == 0)
            return Collections.emptyMap();

        Map<String, String> dataMap = new HashMap<>(size);
        for (String node : nodeList) {
            String nodePath = path + "/" + node;
            try {
                String nodeData = new String(keeper.getData(nodePath, false, null));
                dataMap.put(node, nodeData);
            } catch (KeeperException | InterruptedException e) {
                LOGGER.debug("ZooKeeper get node Data path={} failed. Cause: {}", nodePath, e.getMessage());
            }
        }
        return dataMap;
    }

    /**
     * 获取指定路径下所有子节点的名称和数据列表
     * 
     * @param keeper
     * @param path
     * @param nodeList
     * @return
     */
    public static Collection<Pair<String, String>> listChildren(final ZooKeeper keeper, String path,
            Collection<String> nodeList) {
        int size = nodeList.size();
        if (size == 0)
            return Collections.emptyList();

        List<Pair<String, String>> nodePairs = new ArrayList<>(size);
        for (String node : nodeList) {
            String nodePath = path + "/" + node;
            try {
                String nodeData = new String(keeper.getData(nodePath, false, null));
                nodePairs.add(new Pair<>(node, nodeData));
            } catch (KeeperException | InterruptedException e) {
                LOGGER.debug("ZooKeeper get node Data path={} failed. Cause: {}", nodePath, e);
            }
        }
        return nodePairs;
    }

    /**
     * 获取指定路径下所有子节点的数据
     * 
     * @param keeper
     * @param path
     * @return
     */
    public static Collection<String> listChildrenData(final ZooKeeper keeper, String path) {
        return listChildrenData(keeper, path, listChildrenNode(keeper, path, null));
    }

    /**
     * 获取指定路径下所有子节点的数据
     * 
     * @param keeper
     * @param path
     * @param nodeList
     * @return
     */
    public static Collection<String> listChildrenData(final ZooKeeper keeper, String path,
            Collection<String> nodeList) {
        int size = nodeList.size();
        if (size == 0)
            return Collections.emptyList();

        List<String> dataList = new ArrayList<>(size);
        for (String node : nodeList) {
            String nodePath = path + "/" + node;
            try {
                String nodeData = new String(keeper.getData(nodePath, false, null));
                dataList.add(nodeData);
            } catch (KeeperException | InterruptedException e) {
                LOGGER.debug("ZooKeeper get node data path={} failed. Cause: {}", nodePath, e);
            }
        }
        return dataList;
    }

    /**
     * 获取并监视子节点
     * 
     * @param keeper
     * @param path
     * @param watcher
     * @return 返回子节点的名而不是绝对路径
     */
    public static Collection<String> listChildrenNode(final ZooKeeper keeper, String path, Watcher watcher) {
        try {
            return keeper.getChildren(path, watcher);
        } catch (KeeperException.NoNodeException e) {
            LOGGER.warn("ZooKeeper node(path={}) has no children node", path);
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("ZooKeeper get children node data path={} failed. Cause: {}", path, e);
        }
        return Collections.emptyList();
    }

    /**
     * 设置节点的值
     * 
     * @param keeper
     * @param path
     * @param newData 新的值
     * @return
     */
    public static String setNodeData(final ZooKeeper keeper, String path, String newData) {
        return setNodeData(keeper, path, newData, -1);
    }

    /**
     * 设置节点的值
     * 
     * @param keeper
     * @param path
     * @param newData 新的值
     * @param version 版本号
     * @return
     */
    public static String setNodeData(final ZooKeeper keeper, String path, String newData, int version) {
        try {
            keeper.setData(path, newData.getBytes(), version);
            return newData;
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error("ZooKeeper set node data path={} failed. Cause: {}", path, e);
        }
        return null;
    }

    /**
     * CAS替换节点的值
     * 
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
            return currentData.equals(oldData) && (currentNodeVersion + 1) == keeper
                    .setData(path, newData.getBytes(), currentNodeVersion).getVersion();
        } catch (KeeperException | InterruptedException e) {
            LOGGER.error(e.getMessage());
            return false;
        }
    }
}