package conglin.clrpc.common.util.atomic;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.exception.TransactionException;

public interface TransactionHelper extends Destroyable {
    // 事务状态
    String PREPARE = "PREPARE"; // 准备
    String COMMIT = "COMMIT"; // 提交
    String ABORT = "ABORT"; // 中止

   /**
     * 在原子服务上注册一个 事务ID 的临时节点
     * 该节点的值设为 {@link TransactionHelper#PREPARE}
     * @param transactionId
     * @throws TransactionException
     */
    void begin(Long transactionId) throws TransactionException;

    /**
     * 在原子服务上注册一个 {@code subPath} 的临时节点
     * 该节点的值设为 {@link TransactionHelper#PREPARE}
     * @param subPath
     * @throws TransactionException
     */
    void begin(String subPath) throws TransactionException;

      /**
     * 在原子服务上的 事务ID 的临时节点上创建 子节点
     * 该节点的值设为 {@link TransactionHelper#PREPARE}
     * @param transactionId
     * @param serialNumber
     * @throws TransactionException
     */
    void prepare(Long transactionId, Integer serialNumber) throws TransactionException;

    /**
     * 原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 该节点的值为 {@link TransactionHelper#PREPARE}
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void prepare(String subPath, String serial) throws TransactionException;

    /**
     * 修改原子服务上的 事务ID 的临时节点上 子节点值
     * 使用CAS将子节点的值由{@link TransactionHelper#PREPARE} 
     * 修改为 {@link TransactionHelper#COMMIT}
     * @param transactionId
     * @param serialNumber
     * @return 
     */
    boolean sign(Long transactionId, Integer serialNumber);

    /**
     * 修改原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 使用CAS将子节点的值由{@link TransactionHelper#PREPARE} 修改为 {@link TransactionHelper#COMMIT}
     * @param subPath
     * @param serial
     * @return 
     */
    boolean sign(String subPath, String serial);

    /**
     * 修改原子服务上的 事务ID 的临时节点上的 子节点
     * 的值修改为 {@link TransactionHelper#PREPARE}
     * @param transactionId
     * @param serialNumber
     * @throws TransactionException
     */
    void reparepare(Long transactionId, Integer serialNumber) throws TransactionException;

    /**
     * 修改原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 子节点值修改为 {@link TransactionHelper#PREPARE}
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void reprepare(String subPath, String serial) throws TransactionException;

    /**
     * 监视提交（非阻塞方法）
     * 监视原子服务的 事务ID 的临时节点上的值
     * 若改变为 {@link TransactionHelper#COMMIT} 则执行 {@link Callback#success(Object)} 方法
     * 若改变为 {@link TransactionHelper#ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * @param transactionId
     * @param callback 回调函数
     * @throws TransactionException
     */
    void watch(Long transactionId, Callback callback) throws TransactionException;

        /**
     * 监视提交（非阻塞方法）
     * 监视原子服务的 {@code subPath} 的临时节点上的值
     * 若改变为 {@link TransactionHelper#COMMIT} 则执行 {@link Callback#success(Object)} 方法
     * 若改变为 {@link TransactionHelper#ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * @param subPath
     * @param callback 回调函数
     * @throws TransactionException
     */
    void watch(String subPath, Callback callback) throws TransactionException;

    /**
     * 中止
     * 修改原子服务上的 事务ID 的临时节点值修改为 {@link TransactionHelper#ABORT}
     * @param transactionId
     * @throws TransactionException
     */
    void abort(Long transactionId) throws TransactionException;

    /**
     * 中止
     * 修改原子服务上的 {@code subPath} 的临时节点上 子节点
     * 值修改为 {@link TransactionHelper#ABORT}
     * @param subPath
     * @throws TransactionException
     */
    void abort(String subPath) throws TransactionException;

    /**
     * 提交
     * 修改原子服务上的 事务ID 的临时节点值修改为 {@link TransactionHelper#COMMIT}
     * @param transactionId
     * @throws TransactionException
     */
    void commit(Long transactionId) throws TransactionException;

    /**
     * 提交
     * 修改原子服务上的 {@code subPath} 的临时节点上 子节点值修改为 {@link TransactionHelper#DONE}
     * @param subPath
     * @throws TransactionException
     */
    void commit(String subPath) throws TransactionException;

    
}