package conglin.clrpc.thirdparty.zookeeper.util;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.WatcherType;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.TransactionHelper;
import conglin.clrpc.common.util.concurrent.CountLatch;
import conglin.clrpc.thirdparty.zookeeper.AbstractZooKeeperService;

public class ZooKeeperTransactionHelper extends AbstractZooKeeperService implements TransactionHelper {

    public ZooKeeperTransactionHelper(UrlScheme url) {
        super(url, "transaction");
    }

    @Override
    public void begin(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建节点
        if (ZooKeeperUtils.createNode(keeperInstance.instance(), nodePath, TransactionState.PREPARE.toString()) == null)
            throw new TransactionException("Transaction begin failed. (path = " + nodePath + ")");
    }

    @Override
    public void prepare(String path) throws TransactionException {
        String nodePath = rootPath + "/" + path;
        // 创建临时子节点
        if (ZooKeeperUtils.createNode(keeperInstance.instance(), nodePath, TransactionState.PREPARE.toString()) == null)
            throw new TransactionException("Transaction execute failed. (sub_path = " + nodePath + " )");
    }

    @Override
    public boolean sign(String path) {
        String lockPath = rootPath + "/" + path + "/lock";
        return ZooKeeperUtils.createNode(keeperInstance.instance(), lockPath, CreateMode.EPHEMERAL) != null;
    }

    @Override
    public void reprepare(String path) throws TransactionException {
        String lockPath = rootPath + "/" + path + "/lock";
        ZooKeeperUtils.deleteNode(keeperInstance.instance(), lockPath);
    }

    @Override
    public void watch(String path, Callback callback) throws TransactionException {
        String subnodePath = rootPath + "/" + path;
        Watcher watcher = event -> {
            String newState = ZooKeeperUtils.watchNode(keeperInstance.instance(), subnodePath, null);

            if (Event.EventType.NodeDataChanged == event.getType()) {
                TransactionState state = TransactionState.valueOf(newState);
                if (state == TransactionState.COMMIT) {
                    callback.success(null);
                } else if (state == TransactionState.ABORT) {
                    callback.fail(null);
                }
            }
        };

        String curState = ZooKeeperUtils.watchNode(keeperInstance.instance(), subnodePath, watcher);
        TransactionState state = TransactionState.valueOf(curState);
        if (curState == null)
            throw new TransactionException("Watch failed. (sub_path = " + path + ") ");
        if (state == TransactionState.COMMIT) { // 请求状态已经更改为 COMMIT
            // 直接移除watch即可
            ZooKeeperUtils.removeWatcher(keeperInstance.instance(), subnodePath, watcher, WatcherType.Data);
            callback.success(null);
        } else if (state == TransactionState.ABORT) { // 请求状态已经更改为 ABORT
            // 直接移除watch即可
            ZooKeeperUtils.removeWatcher(keeperInstance.instance(), subnodePath, watcher, WatcherType.Data);
            callback.fail(null);
        }
    }

    @Override
    public void precommit(String path) throws TransactionException {
        if (updateState(path, TransactionState.PRECOMMIT) == null) {
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
        Collection<String> children = ZooKeeperUtils.listChildrenNode(keeperInstance.instance(), nodePath, null);
        CountLatch latch = new CountLatch(children.size());
        children.forEach(node -> {
            String subnodePath = nodePath + "/" + node;
            Watcher watcher = event -> {
                String newState = ZooKeeperUtils.watchNode(keeperInstance.instance(), subnodePath, null);
                if (Event.EventType.NodeDataChanged == event.getType()) {
                    TransactionState state = TransactionState.valueOf(newState);
                    if (state == TransactionState.PRECOMMIT) {
                        latch.countDown();
                    } else if (state == TransactionState.ABORT) {
                        latch.clear();
                    }
                }
            };

            String curState = ZooKeeperUtils.watchNode(keeperInstance.instance(), subnodePath, watcher);
            TransactionState state = TransactionState.valueOf(curState);
            if (state == TransactionState.PRECOMMIT) {
                latch.countDown();
                ZooKeeperUtils.removeWatcher(keeperInstance.instance(), subnodePath, watcher, WatcherType.Data);
            } else if (state == TransactionState.ABORT) {
                latch.clear();
                ZooKeeperUtils.removeWatcher(keeperInstance.instance(), subnodePath, watcher, WatcherType.Data);
            }
        });
        return latch;
    }

    @Override
    public void abort(String path) throws TransactionException {
        if (updateState(path, TransactionState.ABORT) == null) {
            throw new TransactionException("Abort failed. (sub_path = " + path + ") ");
        }
    }

    @Override
    public void commit(String path) throws TransactionException {
        if (updateState(path, TransactionState.COMMIT) == null) {
            throw new TransactionException("Commit failed. (sub_path = " + path + ") ");
        }
    }

    /**
     * 更新原子服务上的节点状态
     * 
     * @param subPath
     * @param state
     */
    protected String updateState(String subPath, TransactionState state) {
        return ZooKeeperUtils.setNodeData(keeperInstance.instance(), rootPath + "/" + subPath, state.toString());
    }
}