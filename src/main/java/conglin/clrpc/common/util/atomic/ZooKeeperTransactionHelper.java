package conglin.clrpc.common.util.atomic;

import java.util.concurrent.CountDownLatch;

import javax.security.auth.DestroyFailedException;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.Watcher.Event;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.ZooKeeperUtils;

public class ZooKeeperTransactionHelper extends ZooKeeperAtomicService implements TransactionHelper {

    public ZooKeeperTransactionHelper() {
        super("/transaction");
    }

    @Override
    public void begin(Long transactionId) throws TransactionException {
        begin(transactionId.toString());
    }

    @Override
    public void begin(String subPath) throws TransactionException {
        // 创建临时节点
        if (ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath, PREPARE, CreateMode.EPHEMERAL) == null)
            throw new TransactionException("Transaction begin failed. (sub_path = " + subPath + ")");
    }

    @Override
    public void clear(Long transactionId) throws TransactionException {
        clear(transactionId.toString());
    }

    @Override
    public void clear(String subPath) throws TransactionException {
        if (ZooKeeperUtils.deleteNode(keeper, rootPath + "/" + subPath) == null)
            throw new TransactionException("Transaction clear failed. (sub_path = " + subPath + ")");
    }

    @Override
    public void prepare(Long transactionId, Integer serialNumber) throws TransactionException {
        prepare(transactionId.toString(), serialNumber.toString());
    }

    @Override
    public void prepare(String subPath, String serial) throws TransactionException {
        // 创建临时子节点
        if (ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath + "/" + serial, PREPARE,
                CreateMode.EPHEMERAL) == null)
            throw new TransactionException(
                    "Transaction execute failed. (sub_path = " + subPath + ", serial=" + serial + " )");
    }

    @Override
    public boolean sign(Long transactionId, Integer serialNumber) {
        return sign(transactionId.toString(), serialNumber.toString());
    }

    @Override
    public boolean sign(String subPath, String serial) {
        return casUpateSate(subPath + "/" + serial, PREPARE, DONE);
    }

    @Override
    public void reparepare(Long transactionId, Integer serialNumber) throws TransactionException {
        reprepare(transactionId.toString(), serialNumber.toString());
    }

    @Override
    public void reprepare(String subPath, String serial) throws TransactionException {
        try {
            updateState(subPath + "/" + serial, PREPARE);
        } catch (KeeperException | InterruptedException e) {
            throw new TransactionException("Reprepare failed. (sub_path = " + subPath + ") " + e.getMessage());
        }
    }

    @Override
    public boolean watch(Long transactionId) throws TransactionException {
        return watch(transactionId.toString());
    }

    @Override
    public boolean watch(String subPath) throws TransactionException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            while (latch.getCount() != 0) {
                String curState = ZooKeeperUtils.watchNode(keeper, rootPath + "/" + subPath, event -> {
                    if (Event.EventType.NodeDataChanged == event.getType())
                        latch.countDown();
                });

                if (curState.equals(DONE)) {
                    return true;
                } else if (curState.equals(ROLLBACK)) {
                    return false;
                } else { // Prepapre
                    latch.await();
                }
            }
        } catch (KeeperException | InterruptedException e) {
            throw new TransactionException("Watch failed. (sub_path = " + subPath + ") " + e.getMessage());
        }
        return false;
    }

    public void watchAsync(Long transactionId, Callback<Object> callback) throws TransactionException {
        watchAsync(transactionId.toString(), callback);
    }

    public void watchAsync(String subPath, Callback<Object> callback) throws TransactionException {

        Watcher watcher = event -> {
            String newState;
            try {
                newState = ZooKeeperUtils.watchNode(keeper, subPath, null);
            } catch (KeeperException | InterruptedException e) {
                throw new TransactionException("Watch failed. (sub_path = " + subPath + ") " + e.getMessage());
            }

            if(Event.EventType.NodeDataChanged == event.getType()){
                if(newState.equals(DONE)){
                    callback.success(null);
                } else if(newState.equals(ROLLBACK)) {
                    callback.fail(null);
                }
            }
        };

        try{
            String curState = ZooKeeperUtils.watchNode(keeper, rootPath + "/" + subPath, watcher);
            if(curState.equals(DONE)){
                callback.success(null);
            } else if(curState.equals(ROLLBACK)) {
                callback.fail(null);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new TransactionException("Watch failed. (sub_path = " + subPath + ") "+ e.getMessage());
        }
    }

    @Override
    public void rollback(Long transactionId) throws TransactionException {
        rollback(transactionId.toString());
    }

    @Override
    public void rollback(String subPath) throws TransactionException {
        try {
            updateState(subPath, ROLLBACK);
        } catch (KeeperException | InterruptedException e) {
            throw new TransactionException("Rollback failed. (sub_path = " + subPath + ") "+ e.getMessage());
        }
    }

    @Override
    public void commit(Long transactionId) throws TransactionException {
        commit(transactionId.toString());
    }

    @Override
    public void commit(String subPath) throws TransactionException {
        try {
            updateState(subPath, DONE);
        } catch (KeeperException | InterruptedException e) {
            throw new TransactionException("Commit failed. (sub_path = " + subPath + ") "+ e.getMessage());
        }
    }

    /**
     * 更新原子服务上的节点状态
     * @param subPath
     * @param state
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected void updateState(String subPath, String state) throws KeeperException, InterruptedException {
        ZooKeeperUtils.setNodeData(keeper, rootPath + "/" + subPath, state);
    }

    /**
     * 使用CAS更新原子服务节点状态
     * @param subPath
     * @param oldState
     * @param newState
     * @return
     */
    protected boolean casUpateSate(String subPath, String oldState, String newState){
        return ZooKeeperUtils.compareAndSetNodeData(keeper, rootPath + "/" + subPath, oldState, newState);
    }

    @Override
    public void destroy() throws DestroyFailedException {
        super.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return super.isDestroyed();
    }
}