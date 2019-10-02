package conglin.clrpc.common.util.atomic;

import conglin.clrpc.common.exception.TransactionException;

public interface TransactionHelper{

    /**
     * 在原子服务上注册一个 {@link TransactionRequest#getRequestId()} 的临时节点
     * @param requestId
     * @throws TransactionException
     */
    void begin(Long requestId) throws TransactionException;

    /**
     * 在原子服务上注册一个 {@code subPath} 的临时节点
     * @param subPath
     * @throws TransactionException
     */
    void begin(String subPath) throws TransactionException;

    /**
     * 删除原子服务上的 {@link TransactionRequest#getRequestId()} 的临时节点
     * @param requestId
     * @throws TransactionException
     */
    void clear(Long requestId) throws TransactionException;

    /**
     * 删除原子服务上的 {@code subPath} 的临时节点
     * @param subPath
     * @throws TransactionException
     */
    void clear(String subPath) throws TransactionException;

    /**
     * 原子服务上的 {@link TransactionRequest#getRequestId()} 的临时节点上创建 序列号
     * {@link TransactionRequest#getSerialNumber()} 子节点
     * @param requestId
     * @param serialNumber
     * @throws TransactionException
     */
    void execute(Long requestId, Integer serialNumber) throws TransactionException;

    /**
     * 原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void execute(String subPath, String serial) throws TransactionException;

    /**
     * 回滚
     * @param requestId
     * @throws TransactionException
     */
    void rollback(Long requestId) throws TransactionException;

    /**
     * 回滚
     * @param subPath
     * @throws TransactionException
     */
    void rollback(String subPath) throws TransactionException;

    /**
     * 提交
     * @param requestId
     * @throws TransactionException
     */
    void commit(Long requestId) throws TransactionException;

    /**
     * 提交
     * @param subPath
     * @throws TransactionException
     */
    void commit(String subPath) throws TransactionException;

    /**
     * 销毁 {@code TransactionHelper} 
     */
    void destroy();
}