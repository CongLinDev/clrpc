package conglin.clrpc.common.util.atomic;

import conglin.clrpc.common.exception.TransactionException;

public interface TransactionHelper{
    // 事务状态
    static final String PREPARE = "PREPARE";
    static final String DONE = "DONE";
    static final String ROLLBACK = "ROLLBACK";

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
     * 子节点的值为 {@link TransactionHelper#PREPARE}
     * @param requestId
     * @param serialNumber
     * @throws TransactionException
     */
    void prepare(Long requestId, Integer serialNumber) throws TransactionException;

    /**
     * 原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 子节点的值为 {@link TransactionHelper#PREPARE}
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void prepare(String subPath, String serial) throws TransactionException;

    /**
     * 修改原子服务上的 {@link TransactionRequest#getRequestId()} 的临时节点上
     * {@link TransactionRequest#getSerialNumber()} 子节点值
     * 使用CAS将子节点的值由{@link TransactionHelper#PREPARE} 修改为 {@link TransactionHelper#DONE}
     * @param requestId
     * @param serialNumber
     * @return 
     */
    boolean sign(Long requestId, Integer serialNumber);

    /**
     * 修改原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 使用CAS将子节点的值由{@link TransactionHelper#PREPARE} 修改为 {@link TransactionHelper#DONE}
     * @param subPath
     * @param serial
     * @return 
     */
    boolean sign(String subPath, String serial);

    /**
     * 修改原子服务上的 {@link TransactionRequest#getRequestId()} 的临时节点上
     * {@link TransactionRequest#getSerialNumber()} 子节点值修改为 {@link TransactionHelper#PREPARE}
     * @param requestId
     * @param serialNumber
     * @throws TransactionException
     */
    void reparepare(Long requestId, Integer serialNumber) throws TransactionException;

    /**
     * 修改原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 子节点值修改为 {@link TransactionHelper#PREPARE}
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void reprepare(String subPath, String serial) throws TransactionException;

    /**
     * 监视提交（阻塞方法）
     * @param requestId
     * @return 是否可以提交
     * @throws TransactionException
     */
    boolean watch(Long requestId) throws TransactionException;

    /**
     * 监视提交（阻塞方法）
     * @param subPath
     * @return 是否可以提交
     * @throws TransactionException
     */
    boolean watch(String subPath) throws TransactionException;

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