package conglin.clrpc.common.util.atomic;

import conglin.clrpc.common.exception.TransactionException;

public interface TransactionHelper{

    /**
     * 在原子服务上注册一个 {@link TransactionRequest#getRequestId()} 的临时节点
     * @param requestId
     */
    void begin(Long requestId);

    /**
     * 删除原子服务上的 {@link TransactionRequest#getRequestId()} 的临时节点
     * @param requestId
     */
    void clear(Long requestId);

    /**
     * 原子服务上的 {@link TransactionRequest#getRequestId()} 的临时节点上创建 序列号
     * {@link TransactionRequest#getSerialNumber()} 子节点
     * @param requestId
     * @param serialNumber
     * @return
     */
    boolean execute(Long requestId, Integer serialNumber);

    /**
     * 回滚
     * @param requestId
     * @throws TransactionException
     */
    void rollback(Long requestId) throws TransactionException;

    /**
     * 提交
     * @param requestId
     * @throws TransactionException
     */
    void commit(Long requestId) throws TransactionException;

    /**
     * 销毁
     */
    void destroy();
}