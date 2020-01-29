package conglin.clrpc.common.util.atomic;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.TransactionException;

public interface TransactionHelper {
    // 事务状态
    String PREPARE = "PREPARE"; // 准备
    String SIGN = "SIGN"; // 占用
    String COMMIT = "COMMIT"; // 提交
    String ABORT = "ABORT"; // 中止

    /**
     * 在原子服务上注册一个 事务ID 的节点 该节点的值设为 {@link TransactionHelper#PREPARE}
     * 
     * @param transactionId
     * @throws TransactionException
     */
    void begin(long transactionId) throws TransactionException;

    /**
     * 在原子服务上注册一个 {@code subPath} 的节点 该节点的值设为 {@link TransactionHelper#PREPARE}
     * 
     * @param subPath
     * @throws TransactionException
     */
    void begin(String subPath) throws TransactionException;

    /**
     * 在原子服务上的 事务ID 的节点上创建 临时子节点
     * 
     * 该节点的值设为 {@link TransactionHelper#SIGN}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    void prepare(long transactionId, int serialId) throws TransactionException;

    /**
     * 原子服务上的 {@code subPath} 的节点上创建 序列号 {@code serial} 临时子节点
     * 
     * 该节点的值为 {@link TransactionHelper#SIGN}
     * 
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void prepare(String subPath, String serial) throws TransactionException;

    /**
     * 修改原子服务上的 事务ID 的节点 子节点值
     * 
     * 使用CAS将子节点的值由 {@link TransactionHelper#PREPARE} 修改为
     * {@link TransactionHelper#SIGN}
     * 
     * @param transactionId
     * @param serialId
     * @return
     */
    boolean sign(long transactionId, int serialId);

    /**
     * 修改原子服务上的 {@code subPath} 的 序列号 {@code serial} 子节点值
     * 
     * 使用CAS将子节点的值由 {@link TransactionHelper#PREPARE} 修改为
     * {@link TransactionHelper#SIGN}
     * 
     * @param subPath
     * @param serial
     * @return
     */
    boolean sign(String subPath, String serial);

    /**
     * 修改原子服务上的 事务ID 的节点上 子节点 的值修改为 {@link TransactionHelper#SIGN}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    void reparepare(long transactionId, int serialId) throws TransactionException;

    /**
     * 修改原子服务上的 {@code subPath} 的 序列号 {@code serial} 子节点值修改为
     * {@link TransactionHelper#SIGN}
     * 
     * @param subPath
     * @param serial
     * @throws TransactionException
     */
    void reprepare(String subPath, String serial) throws TransactionException;

    /**
     * 监视提交（非阻塞方法） 监视原子服务的 事务ID 的节点上的值
     * 
     * 若改变为 {@link TransactionHelper#COMMIT} 则执行 {@link Callback#success(Object)} 方法
     * 若改变为 {@link TransactionHelper#ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * 
     * @param transactionId
     * @param callback      回调函数
     * @throws TransactionException
     */
    void watch(long transactionId, Callback callback) throws TransactionException;

    /**
     * 监视提交（非阻塞方法） 监视原子服务的 事务ID 节点上 临时子节点的值
     * 
     * 若改变为 {@link TransactionHelper#COMMIT} 则执行 {@link Callback#success(Object)} 方法
     * 若改变为 {@link TransactionHelper#ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * 
     * 若 {@code serialId} 为 0 则调用 {@link TransactionHelper#watch(long, Callback)} 方法
     * 
     * @param transactionId
     * @param serialId
     * @param callback
     * @throws TransactionException
     */
    void watch(long transactionId, int serialId, Callback callback) throws TransactionException;

    /**
     * 监视提交（非阻塞方法） 监视原子服务的 {@code subPath} 的节点上的值
     * 
     * 若改变为 {@link TransactionHelper#COMMIT} 则执行 {@link Callback#success(Object)} 方法
     * 若改变为 {@link TransactionHelper#ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * 
     * @param subPath
     * @param callback 回调函数
     * @throws TransactionException
     */
    void watch(String subPath, Callback callback) throws TransactionException;

    /**
     * 中止 修改原子服务上的 事务ID 的节点值修改为 {@link TransactionHelper#ABORT}
     * 
     * @param transactionId
     * @throws TransactionException
     */
    void abort(long transactionId) throws TransactionException;

    /**
     * 中止 修改原子服务上的 事务ID 的临时子节点值修改为 {@link TransactionHelper#ABORT}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    void abort(long transactionId, int serialId) throws TransactionException;

    /**
     * 中止 修改原子服务上的 {@code subPath} 的节点值修改为 {@link TransactionHelper#ABORT}
     * 
     * @param subPath
     * @throws TransactionException
     */
    void abort(String subPath) throws TransactionException;

    /**
     * 提交 修改原子服务上的 事务ID 的节点值修改为 {@link TransactionHelper#COMMIT}
     * 
     * @param transactionId
     * @throws TransactionException
     */
    void commit(long transactionId) throws TransactionException;

    /**
     * 提交 修改原子服务上的 事务ID 的临时子节点值修改为 {@link TransactionHelper#COMMIT}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    void commit(long transactionId, int serialId) throws TransactionException;

    /**
     * 提交 修改原子服务上的 {@code subPath} 的节点值修改为 {@link TransactionHelper#DONE}
     * 
     * @param subPath
     * @throws TransactionException
     */
    void commit(String subPath) throws TransactionException;

}