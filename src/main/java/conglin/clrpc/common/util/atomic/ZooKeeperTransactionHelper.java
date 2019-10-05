package conglin.clrpc.common.util.atomic;

import java.util.concurrent.CountDownLatch;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.ZooKeeperUtils;

public class ZooKeeperTransactionHelper extends ZooKeeperAtomicService implements TransactionHelper {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperTransactionHelper.class);

    public ZooKeeperTransactionHelper() {
        super("/transaction");
    }

    @Override
    public void begin(Long requestId) throws TransactionException {
        begin(requestId.toString());
    }

    @Override
    public void begin(String subPath) throws TransactionException {
        // 创建临时节点
        if (ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath, PREPARE, CreateMode.EPHEMERAL) == null)
            throw new TransactionException("Transaction begin failed. (sub_path = " + subPath + ")");
    }

    @Override
    public void clear(Long requestId) throws TransactionException {
        clear(requestId.toString());
    }

    @Override
    public void clear(String subPath) throws TransactionException {
        if (ZooKeeperUtils.deleteNode(keeper, rootPath + "/" + subPath) == null)
            throw new TransactionException("Transaction clear failed. (sub_path = " + subPath + ")");
    }

    @Override
    public void prepare(Long requestId, Integer serialNumber) throws TransactionException {
        prepare(requestId.toString(), serialNumber.toString());
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
    public boolean sign(Long requestId, Integer serialNumber) {
        return sign(requestId.toString(), serialNumber.toString());
    }

    @Override
    public boolean sign(String subPath, String serial) {
        return casUpateSate(subPath + "/" + serial, PREPARE, DONE);
    }

    @Override
    public void reparepare(Long requestId, Integer serialNumber) throws TransactionException {
        reprepare(requestId.toString(), serialNumber.toString());
    }

    @Override
    public void reprepare(String subPath, String serial) throws TransactionException {
        try {
            updateState(subPath + "/" + serial, PREPARE);
        } catch (KeeperException | InterruptedException e) {
            String errorDesc = "Reprepare failed. (sub_path = " + subPath + ") " + e.getMessage();
            log.error(errorDesc);
            throw new TransactionException(errorDesc);
        }
    }

    @Override
    public void watch(Long requestId) throws TransactionException {
        watch(requestId.toString());
    }

    @Override
    public void watch(String subPath) throws TransactionException {
        CountDownLatch latch = new CountDownLatch(1);
        try {
            ZooKeeperUtils.watchNode(keeper, rootPath + "/" + subPath, event -> {
                if(Event.EventType.NodeDataChanged == event.getType())
                    latch.countDown();
            });
            latch.await();
        } catch (KeeperException | InterruptedException e) {
            String errorDesc = "Watch failed. (sub_path = " + subPath + ") "+ e.getMessage();
            log.error(errorDesc);
            throw new TransactionException(errorDesc);
        }
    }

    @Override
    public void rollback(Long requestId) throws TransactionException {
        rollback(requestId.toString());
    }

    @Override
    public void rollback(String subPath) throws TransactionException {
        try {
            updateState(subPath, ROLLBACK);
        } catch (KeeperException | InterruptedException e) {
            String errorDesc = "Rollback failed. (sub_path = " + subPath + ") "+ e.getMessage();
            log.error(errorDesc);
            throw new TransactionException(errorDesc);
        }
    }

    @Override
    public void commit(Long requestId) throws TransactionException {
        commit(requestId.toString());
    }

    @Override
    public void commit(String subPath) throws TransactionException {
        try {
            updateState(subPath, DONE);
        } catch (KeeperException | InterruptedException e) {
            String errorDesc = "Commit failed. (sub_path = " + subPath + ") "+ e.getMessage();
            log.error(errorDesc);
            throw new TransactionException(errorDesc);
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
    public void destroy() {
        super.destroy();
    }
}