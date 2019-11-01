package conglin.clrpc.common.util.atomic;

import javax.security.auth.Destroyable;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.TransactionException;

public interface TransactionHelper extends Destroyable {
    // 事务状态
    String PREPARE = "PREPARE";
    String DONE = "DONE";
    String ROLLBACK = "ROLLBACK";

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
     * 删除原子服务上的 事务ID 的临时节点
     * @param transactionId
     * @throws TransactionException
     */
    void clear(Long transactionId) throws TransactionException;

    /**
     * 删除原子服务上的 {@code subPath} 的临时节点
     * @param subPath
     * @throws TransactionException
     */
    void clear(String subPath) throws TransactionException;

    /**
     * 在原子服务上的 事务ID 的临时节点上创建 子节点
     * 子节点的值为 {@link TransactionHelper#PREPARE}
     * @param transactionId
     * @param serialNumber
     * @throws TransactionException
     */
    void prepare(Long transactionId, Integer serialNumber) throws TransactionException;

    /**
     * 原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 子节点的值为 {@link TransactionHelper#PREPARE}
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void prepare(String subPath, String serial) throws TransactionException;

    /**
     * 修改原子服务上的 事务ID 的临时节点上 子节点值
     * 使用CAS将子节点的值由{@link TransactionHelper#PREPARE} 修改为 {@link TransactionHelper#DONE}
     * @param transactionId
     * @param serialNumber
     * @return 
     */
    boolean sign(Long transactionId, Integer serialNumber);

    /**
     * 修改原子服务上的 {@code subPath} 的临时节点上创建 序列号 {@code serial} 子节点
     * 使用CAS将子节点的值由{@link TransactionHelper#PREPARE} 修改为 {@link TransactionHelper#DONE}
     * @param subPath
     * @param serial
     * @return 
     */
    boolean sign(String subPath, String serial);

    /**
     * 修改原子服务上的 事务ID 的临时节点上 子节点值修改为 {@link TransactionHelper#PREPARE}
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
     * 监视提交（阻塞方法）
     * 监视原子服务的 事务ID 的临时节点上的值
     * 若改变为 {@link TransactionHelper#DONE} 则返回 {@code true} 通知提交
     * 若改变为 {@link TransactionHelper#ROLLBACK} 则返回 {@code false} 通知回滚
     * @param transactionId
     * @return 是否可以提交
     * @throws TransactionException
     */
    boolean watch(Long transactionId) throws TransactionException;

    /**
     * 监视提交（阻塞方法）
     * 监视原子服务的 {@code subPath} 的临时节点上的值
     * 若改变为 {@link TransactionHelper#DONE} 则返回 {@code true} 通知提交
     * 若改变为 {@link TransactionHelper#ROLLBACK} 则返回 {@code false} 通知回滚
     * @param subPath
     * @return 是否可以提交
     * @throws TransactionException
     */
    boolean watch(String subPath) throws TransactionException;

    /**
     * 监视提交（非阻塞方法）
     * 监视原子服务的 事务ID 的临时节点上的值
     * 若改变为 {@link TransactionHelper#DONE} 则执行 {@link Callback#success(Object)} 方法
     * 若改变为 {@link TransactionHelper#ROLLBACK} 则执行 {@link Callback#fail(Object)} 方法
     * @param transactionId
     * @param callback 回调函数
     * @throws TransactionException
     */
    void watchAsync(Long transactionId, Callback<Object> callback) throws TransactionException;

    /**
     * 监视提交（非阻塞方法）
     * 监视原子服务的 {@code subPath} 的临时节点上的值
     * 若改变为 {@link TransactionHelper#DONE} 则执行 {@link Callback#success(Object)} 方法
     * 若改变为 {@link TransactionHelper#ROLLBACK} 则执行 {@link Callback#fail(Object)} 方法
     * @param subPath
     * @param callback 回调函数
     * @throws TransactionException
     */
    void watchAsync(String subPath, Callback<Object> callback) throws TransactionException;

    /**
     * 回滚
     * 修改原子服务上的 事务ID 的临时节点上 子节点值修改为 {@link TransactionHelper#ROLLBACK}
     * @param transactionId
     * @throws TransactionException
     */
    void rollback(Long transactionId) throws TransactionException;

    /**
     * 回滚
     * 修改原子服务上的 {@code subPath} 的临时节点上 子节点值修改为 {@link TransactionHelper#ROLLBACK}
     * @param subPath
     * @throws TransactionException
     */
    void rollback(String subPath) throws TransactionException;

    /**
     * 提交
     * 修改原子服务上的 事务ID 的临时节点上 子节点值修改为 {@link TransactionHelper#DONE}
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