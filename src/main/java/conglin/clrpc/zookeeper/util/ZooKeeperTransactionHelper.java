package conglin.clrpc.zookeeper.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.WatcherType;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.Url;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.TransactionHelper;
import conglin.clrpc.common.util.concurrent.CountLatch;
import conglin.clrpc.zookeeper.AbstractZooKeeperService;

public class ZooKeeperTransactionHelper extends AbstractZooKeeperService implements TransactionHelper {

    public ZooKeeperTransactionHelper(Url url) {
        super(url, "transaction");
    }

    @Override
    public void begin(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建节点
        if (ZooKeeperUtils.createNode(keeper, nodePath, PREPARE) == null)
            throw new TransactionException("Transaction begin failed. (path = " + nodePath + ")");
    }

    @Override
    public void prepare(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建临时子节点
        if (ZooKeeperUtils.createNode(keeper, nodePath, PREPARE) == null)
            throw new TransactionException("Transaction execute failed. (sub_path = " + nodePath + " )");
    }

    @Override
    public boolean sign(String path) {
        String lockPath = rootPath + "/" + path + "/lock";
        return ZooKeeperUtils.createNode(keeper, lockPath, CreateMode.EPHEMERAL) != null;
    }

    @Override
    public void reprepare(String path) throws TransactionException {
        String lockPath = rootPath + "/" + path + "/lock";
        ZooKeeperUtils.deleteNode(keeper, lockPath);
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
        CountLatch latch = doCheck(path);
        try {
            latch.await();
        } catch (InterruptedException e) {
            throw new TransactionException("Check failed." + e.getMessage());
        }
        return !latch.isClear();
    }

    @Override
    public boolean check(String path, long timeout, TimeUnit unit) throws TransactionException {
        CountLatch latch = doCheck(path);
        try {
            latch.await(timeout, unit);
        } catch (InterruptedException e) {
            latch.clear();
        }
        return !latch.isClear();
    }

    /**
     * 检查的实际工作
     * 
     * @param path
     * @return
     * @throws TransactionException
     */
    protected CountLatch doCheck(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        Collection<String> children = ZooKeeperUtils.listChildrenNode(keeper, nodePath, null);
        CountLatch latch = new CountLatch(children.size());
        children.forEach(node -> {
            String subnodePath = nodePath + "/" + node;
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
        return latch;
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
}