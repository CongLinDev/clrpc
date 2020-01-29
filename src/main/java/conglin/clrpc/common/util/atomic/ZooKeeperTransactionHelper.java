package conglin.clrpc.common.util.atomic;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;
import org.apache.zookeeper.Watcher.WatcherType;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.ZooKeeperUtils;

public class ZooKeeperTransactionHelper extends ZooKeeperAtomicService implements TransactionHelper {

    public ZooKeeperTransactionHelper(PropertyConfigurer configurer) {
        super(configurer, "/transaction");
    }

    @Override
    public void begin(long transactionId) throws TransactionException {
        begin(String.valueOf(transactionId));
    }

    @Override
    public void begin(String subPath) throws TransactionException {
        // 创建节点
        if (ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath, PREPARE, CreateMode.PERSISTENT) == null)
            throw new TransactionException("Transaction begin failed. (sub_path = " + subPath + ")");
    }

    @Override
    public void prepare(long transactionId, int serialId) throws TransactionException {
        prepare(String.valueOf(transactionId), String.valueOf(serialId));
    }

    @Override
    public void prepare(String subPath, String serial) throws TransactionException {
        // 创建临时子节点
        if (ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath + "/" + serial, SIGN,
                CreateMode.EPHEMERAL) == null)
            throw new TransactionException(
                    "Transaction execute failed. (sub_path = " + subPath + ", serial=" + serial + " )");
    }

    @Override
    public boolean sign(long transactionId, int serialId) {
        return sign(String.valueOf(transactionId), String.valueOf(serialId));
    }

    @Override
    public boolean sign(String subPath, String serial) {
        return casUpateState(subPath + "/" + serial, PREPARE, SIGN);
    }

    @Override
    public void reparepare(long transactionId, int serialId) throws TransactionException {
        reprepare(String.valueOf(transactionId), String.valueOf(serialId));
    }

    @Override
    public void reprepare(String subPath, String serial) throws TransactionException {
        if (updateState(subPath + "/" + serial, PREPARE) == null) {
            throw new TransactionException("Reprepare failed. (sub_path = " + subPath + ") ");
        }
    }

    @Override
    public void watch(long transactionId, Callback callback) throws TransactionException {
        watch(String.valueOf(transactionId), callback);
    }

    @Override
    public void watch(long transactionId, int serialId, Callback callback) throws TransactionException {
        if (serialId == 0) {
            watch(transactionId, callback);
        } else {
            watch(transactionId + "/" + serialId, callback);
        }
    }

    @Override
    public void watch(String subPath, Callback callback) throws TransactionException {
        String subnodePath = rootPath + "/" + subPath;
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
            throw new TransactionException("Watch failed. (sub_path = " + subPath + ") ");
        if (COMMIT.equals(curState)) { // 请求状态已经更改为 COMMIT
            // 直接移除watch即可
            ZooKeeperUtils.removeWatcher(keeper, subnodePath, watcher, WatcherType.Data);
            callback.success(null);
        } else if (ABORT.equals(curState)) { // 请求状态已经更改为 ABORT
            callback.fail(null);
        }
    }

    @Override
    public void abort(long transactionId) throws TransactionException {
        abort(String.valueOf(transactionId));
    }

    @Override
    public void abort(long transactionId, int serialId) throws TransactionException {
        abort(transactionId + "/" + serialId);
    }

    @Override
    public void abort(String subPath) throws TransactionException {
        if (updateState(subPath, ABORT) == null) {
            throw new TransactionException("Abort failed. (sub_path = " + subPath + ") ");
        }
    }

    @Override
    public void commit(long transactionId) throws TransactionException {
        commit(String.valueOf(transactionId));
    }

    @Override
    public void commit(long transactionId, int serialId) throws TransactionException {
        commit(transactionId + "/" + serialId);
    }

    @Override
    public void commit(String subPath) throws TransactionException {
        if (updateState(subPath, COMMIT) == null) {
            throw new TransactionException("Commit failed. (sub_path = " + subPath + ") ");
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