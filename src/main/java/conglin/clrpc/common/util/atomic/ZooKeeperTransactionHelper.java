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
    public void begin(Long requestId) {
        begin(requestId.toString());
    }

    /**
     * 在原子服务上注册一个 {@code subPath} 的临时节点
     * 
     * @param subPath
     */
    public void begin(String subPath) {
        // 创建临时节点
        ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath, PREPARE, CreateMode.EPHEMERAL);
    }

    @Override
    public void clear(Long requestId) {
        clear(requestId.toString());
    }

    /**
     * 删除原子服务上的 {@code subPath} 的临时节点
     * 
     * @param subPath
     */
    public void clear(String subPath) {
        ZooKeeperUtils.deleteNode(keeper, rootPath + "/" + subPath);
    }

    @Override
    public boolean execute(Long requestId, Integer serialNumber) {
        return execute(requestId.toString(), serialNumber.toString());
    }

    /**
     * 原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 
     * @param subPath
     * @param serial
     * @return
     */
    public boolean execute(String subPath, String serial) {
        // 创建临时子节点
        return ZooKeeperUtils.createNode(keeper, rootPath + "/" + subPath + "/" + serial, PREPARE,
                CreateMode.EPHEMERAL) != null;
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
    public void rollback(Long requestId) {
        try {
            updateState(rootPath + "/" + requestId, ROLLBACK);
        } catch (KeeperException | InterruptedException e) {
            String errorDesc = "Rollback failed. (requestId = " + requestId + ") "+ e.getMessage();
            log.error(errorDesc);
            throw new TransactionException(errorDesc);
        }

    }

    @Override
    public void commit(Long requestId) {
        try {
            updateState(rootPath + "/" + requestId, DONE);
        } catch (KeeperException | InterruptedException e) {
            String errorDesc = "Commit failed. (requestId = " + requestId + ") "+ e.getMessage();
            log.error(errorDesc);
            throw new TransactionException(errorDesc);
        }
    }

    @Override
    public void destroy() {
        close();
    }
}