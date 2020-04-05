package conglin.clrpc.zookeeper.util;

import java.util.Collection;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.WatcherType;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.TransactionHelper;
import conglin.clrpc.common.util.concurrent.CountLatch;
import conglin.clrpc.zookeeper.AbstractZooKeeperService;

public class ZooKeeperTransactionHelper extends AbstractZooKeeperService implements TransactionHelper {

    public ZooKeeperTransactionHelper(PropertyConfigurer configurer) {
        super("atomicity", configurer, "transaction");
    }

    @Override
    public void begin(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建节点
        if (ZooKeeperUtils.createNode(keeper, nodePath, PREPARE, CreateMode.PERSISTENT) == null)
            throw new TransactionException("Transaction begin failed. (path = " + nodePath + ")");
    }

    @Override
    public void prepare(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建临时子节点
        if (ZooKeeperUtils.createNode(keeper, nodePath, PREPARE, CreateMode.EPHEMERAL) == null)
            throw new TransactionException("Transaction execute failed. (sub_path = " + nodePath + " )");
    }

    @Override
    public boolean sign(String path) {
        return casUpateState(path, PREPARE, SIGN);
    }

    @Override
    public void reprepare(String path) throws TransactionException {
        if (updateState(path, PREPARE) == null) {
            throw new TransactionException("Reprepare failed. (sub_path = " + path + ") ");
        }
    }

    @Override
    public void watch(String path, Callback callback) throws TransactionException {
        String subnodePath = rootPath + "/" + path;
        Watcher watcher = event -> {
            String newState = ZooKeeperUtils.watchNode(keeper, subnodePath, null);

            if (Event.EventType.NodeDataChanged == event.getType()) {
                if (COMMIT.equals(newState)) {
                    callback.success(null);
                } else if (ABORT.equals(newState)) {
                    callback.fail(null);
                }
            }
        };

        String curState = ZooKeeperUtils.watchNode(keeper, subnodePath, watcher);
        if (curState == null)
            throw new TransactionException("Watch failed. (sub_path = " + path + ") ");
        if (COMMIT.equals(curState)) { // 请求状态已经更改为 COMMIT
            // 直接移除watch即可
            ZooKeeperUtils.removeWatcher(keeper, subnodePath, watcher, WatcherType.Data);
            callback.success(null);
        } else if (ABORT.equals(curState)) { // 请求状态已经更改为 ABORT
            // 直接移除watch即可
            ZooKeeperUtils.removeWatcher(keeper, subnodePath, watcher, WatcherType.Data);
            callback.fail(null);
        }
    }

    @Override
    public void precommit(String path) throws TransactionException {
        if (updateState(path, PRECOMMIT) == null) {
            throw new TransactionException("Precommit failed. (sub_path = " + path + ") ");
        }
    }

    @Override
    public boolean check(String path) throws TransactionException {
        String nodePath = rootPath  + "/" + path;
        Collection<String> children = ZooKeeperUtils.listChildrenNode(keeper, nodePath, null);
        CountLatch latch = new CountLatch(children.size());
        try {
            children.forEach(node -> {
                String subnodePath = nodePath + "/" + node;//121
                Watcher watcher = event -> {
                    String newState = ZooKeeperUtils.watchNode(keeper, subnodePath, null);
                    if (Event.EventType.NodeDataChanged == event.getType()) {
                        if (PRECOMMIT.equals(newState)) {
                            latch.countDown();
                        } else if (ABORT.equals(newState)) {
                            latch.clear();
                        }
                    }
                };
                String curState = ZooKeeperUtils.watchNode(keeper, subnodePath, watcher);
                if (PRECOMMIT.equals(curState)) {
                    latch.countDown();
                    ZooKeeperUtils.removeWatcher(keeper, subnodePath, watcher, WatcherType.Data);
                } else if (ABORT.equals(curState)) {
                    latch.clear();
                    ZooKeeperUtils.removeWatcher(keeper, subnodePath, watcher, WatcherType.Data);
                }
            });
            latch.await();
        } catch (InterruptedException e) {
            throw new TransactionException("Check failed." + e.getMessage());
        }
        return !latch.isClear();
    }

    @Override
    public void abort(String path) throws TransactionException {
        if (updateState(path, ABORT) == null) {
            throw new TransactionException("Abort failed. (sub_path = " + path + ") ");
        }
    }

    @Override
    public void commit(String path) throws TransactionException {
        if (updateState(path, COMMIT) == null) {
            throw new TransactionException("Commit failed. (sub_path = " + path + ") ");
        }
    }

    /**
     * 更新原子服务上的节点状态
     * 
     * @param subPath
     * @param state
     */
    protected String updateState(String subPath, String state) {
        return ZooKeeperUtils.setNodeData(keeper, rootPath + "/" + subPath, state);
    }

    /**
     * 使用CAS更新原子服务节点状态
     * 
     * @param subPath
     * @param oldState
     * @param newState
     * @return
     */
    protected boolean casUpateState(String subPath, String oldState, String newState) {
        return ZooKeeperUtils.compareAndSetNodeData(keeper, rootPath + "/" + subPath, oldState, newState);
    }
}