package conglin.clrpc.common.util.atomic;

import org.apache.zookeeper.CreateMode;

import conglin.clrpc.common.util.ZooKeeperUtils;
import conglin.clrpc.transfer.message.TransactionRequest;

public class ZooKeeperTransactionHelper extends ZooKeeperAtomicService {

    // 事务状态
    private static final String PREPARE = "PREPARE";
    // private static final String DONE = "DONE";

    public ZooKeeperTransactionHelper(){
        super("/transaction");
    }

    /**
     * 在原子服务上注册一个 {@link TransactionRequest#getRequestId()} 的临时节点
     * @param requestId
     */
    public void begin(Long requestId){
        // 创建临时节点
        ZooKeeperUtils.createNode(keeper, rootPath + "/" + requestId.toString(), PREPARE, CreateMode.EPHEMERAL);
    }

    /**
     * 删除原子服务上的  {@link TransactionRequest#getRequestId()} 的临时节点
     * @param requestId
     */
    public void clear(Long requestId){
        if(requestId == null) return;
        ZooKeeperUtils.deleteNode(keeper, rootPath + "/" + requestId);
    }

    /**
     * 原子服务上的  {@link TransactionRequest#getRequestId()} 的临时节点上创建
     * 序列号 {@link TransactionRequest#getSerialNumber()} 子节点
     * @param requestId
     * @param serialNumber
     */
    public void call(Long requestId, Integer serialNumber){
        // 创建临时子节点
        ZooKeeperUtils.createNode(keeper, 
            rootPath + "/" + requestId.toString() + "/" + serialNumber.toString(),
            PREPARE,
            CreateMode.EPHEMERAL);
    }
}