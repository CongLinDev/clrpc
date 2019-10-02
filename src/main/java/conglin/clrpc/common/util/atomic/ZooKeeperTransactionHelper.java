package conglin.clrpc.common.util.atomic;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.ZooKeeperUtils;

public class ZooKeeperTransactionHelper extends ZooKeeperAtomicService implements TransactionHelper {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperTransactionHelper.class);

    // 事务状态
    private static final String PREPARE = "PREPARE";
    private static final String DONE = "DONE";
    private static final String ROLLBACK = "ROLLBACK";

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
        if(ZooKeeperUtils.createNode(keeper, 
                rootPath + "/" + subPath,
                PREPARE,
                CreateMode.EPHEMERAL) == null)
            throw new TransactionException("Transaction begin failed. (sub_path = " + subPath + ")");

    }

    @Override
    public void clear(Long requestId) throws TransactionException {
        clear(requestId.toString());
    }

    @Override
    public void clear(String subPath) throws TransactionException {
        if(ZooKeeperUtils.deleteNode(keeper, rootPath + "/" + subPath) == null)
            throw new TransactionException("Transaction clear failed. (sub_path = " + subPath + ")");
    }

    @Override
    public void execute(Long requestId, Integer serialNumber) throws TransactionException {
        execute(requestId.toString(), serialNumber.toString());
    }

    @Override
    public void execute(String subPath, String serial) throws TransactionException {
        // 创建临时子节点
        if(ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath + "/" + serial,
            PREPARE,
            CreateMode.EPHEMERAL) == null)
            throw new TransactionException("Transaction execute failed. (sub_path = " + 
                subPath + ", serial=" + serial + " )");    
    }

    /**
     * 更新原子服务上的节点状态
     * 
     * @param subPath
     * @param state
     * @throws KeeperException
     * @throws InterruptedException
     */
    protected void updateState(String subPath, String state) throws KeeperException, InterruptedException {
        ZooKeeperUtils.setNodeData(keeper, subPath, state);
    }

    @Override
    public void rollback(Long requestId) throws TransactionException {
        rollback(requestId.toString());
    }

    @Override
    public void rollback(String subPath) throws TransactionException {
        try {
            updateState(rootPath + "/" + subPath, ROLLBACK);
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
            updateState(rootPath + "/" + subPath, DONE);
        } catch (KeeperException | InterruptedException e) {
            String errorDesc = "Commit failed. (sub_path = " + subPath + ") "+ e.getMessage();
            log.error(errorDesc);
            throw new TransactionException(errorDesc);
        }
    }

    @Override
    public void destroy() {
        close();
    }
}