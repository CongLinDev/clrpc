package conglin.clrpc.common.util;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.TransactionException;

public interface TransactionHelper {
    // 事务状态
    String PREPARE = "PREPARE"; // 准备
    String SIGN = "SIGN"; // 占用
    String PRECOMMIT = "PRECOMMIT"; // 预提交
    String COMMIT = "COMMIT"; // 提交
    String ABORT = "ABORT"; // 中止

    /**
     * 在原子服务上注册一个 事务ID 的节点 该节点的值设为 {@link #PREPARE}
     * 
     * @param transactionId
     * @throws TransactionException
     */
    default void begin(long transactionId) throws TransactionException {
        begin(String.valueOf(transactionId));
    }

    /**
     * 在原子服务上注册一个 {@code subPath} 的节点 该节点的值设为 {@link #PREPARE}
     * 
     * @param path
     * @throws TransactionException
     */
    void begin(String path) throws TransactionException;

    /**
     * 在原子服务上的 事务ID 的节点上创建 临时子节点
     * 
     * 该节点的值设为 {@link #SIGN}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    default void prepare(long transactionId, int serialId) throws TransactionException {
        prepare(transactionId + "/" + serialId);
    }

    /**
     * 该节点的值为 {@link #SIGN}
     * 
     * @param path
     * @throws TransactionException
     */
    void prepare(String path) throws TransactionException;

    /**
     * 修改原子服务上的 事务ID 的节点 子节点值
     * 
     * 使用CAS将子节点的值由 {@link #PREPARE} 修改为 {@link #SIGN}
     * 
     * @param transactionId
     * @param serialId
     * @return
     */
    default boolean sign(long transactionId, int serialId) {
        return sign(transactionId + "/" + serialId);
    }

    /**
     * 使用CAS将子节点的值由 {@link #PREPARE} 修改为 {@link #SIGN}
     * 
     * @param path
     * @return
     */
    boolean sign(String path);

    /**
     * 修改原子服务上的 事务ID 的节点上 子节点 的值修改为 {@link #SIGN}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    default void reprepare(long transactionId, int serialId) throws TransactionException {
        reprepare(transactionId + "/" + serialId);
    }

    /**
     * 修改原子服务上的 事务ID 的节点上 子节点 的值修改为 {@link #SIGN}
     * 
     * @param path
     * @throws TransactionException
     */
    void reprepare(String path) throws TransactionException;

    /**
     * 监视提交（非阻塞方法） 监视原子服务的 事务ID 的节点上的值
     * 
     * 若改变为 {@link #COMMIT} 则执行 {@link Callback#success(Object)} 方法 若改变为
     * {@link #ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * 
     * @param transactionId
     * @param callback      回调对象
     * @throws TransactionException
     */
    default void watch(long transactionId, Callback callback) throws TransactionException {
        watch(String.valueOf(transactionId), callback);
    }

    /**
     * 监视提交（非阻塞方法） 监视原子服务的 事务ID 节点上 临时子节点的值
     * 
     * 若改变为 {@link #COMMIT} 则执行 {@link Callback#success(Object)} 方法 若改变为
     * {@link #ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * 
     * 若 {@code serialId} 为 0 则调用 {@link #watch(long, Callback)} 方法
     * 
     * @param transactionId
     * @param serialId
     * @param callback      回调对象
     * @throws TransactionException
     */
    default void watch(long transactionId, int serialId, Callback callback) throws TransactionException {
        if (serialId == 0) {
            watch(transactionId, callback);
        } else {
            watch(transactionId + "/" + serialId, callback);
        }
    }

    /**
     * 监视提交（非阻塞方法） 监视原子服务的 {@code path} 的节点上的值
     * 
     * 若改变为 {@link #COMMIT} 则执行 {@link Callback#success(Object)} 方法 若改变为
     * {@link #ABORT} 则执行 {@link Callback#fail(Exception)} 方法
     * 
     * @param path
     * @param callback 回调对象
     * @throws TransactionException
     */
    void watch(String path, Callback callback) throws TransactionException;

    /**
     * 中止 修改原子服务上的 事务ID 的节点值修改为 {@link #ABORT}
     * 
     * @param transactionId
     * @throws TransactionException
     */
    default void abort(long transactionId) throws TransactionException {
        abort(String.valueOf(transactionId));
    }

    /**
     * 中止 修改原子服务上的 事务ID 的临时子节点值修改为 {@link #ABORT}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    default void abort(long transactionId, int serialId) throws TransactionException {
        abort(transactionId + "/" + serialId);
    }

    /**
     * 中止 修改原子服务上的 {@code path} 的节点值修改为 {@link #ABORT}
     * 
     * @param path
     * @throws TransactionException
     */
    void abort(String path) throws TransactionException;

    /**
     * 预提交 修改原子服务上的 {@code path} 的节点值修改为 {@link #PRECOMMIT}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    default void precommit(long transactionId, int serialId) throws TransactionException {
        precommit(transactionId + "/" + serialId);
    }

    /**
     * 预提交 修改原子服务上的 {@code path} 的节点值修改为 {@link #PRECOMMIT}
     * 
     * @param path
     * @throws TransactionException
     */
    void precommit(String path) throws TransactionException;

    /**
     * 检查子节点是否准备好
     * 
     * 若均为 {@link #PRECOMMIT} 则返回 {@code true} 若存在 {@link #ABORT} 则返回 {@code false}
     * 
     * 若不满足上述两种情况，则一直阻塞
     * 
     * @param transactionId
     * @return
     * @throws TransactionException
     */
    default boolean check(long transactionId) throws TransactionException {
        return check(String.valueOf(transactionId));
    }

    /**
     * 检查子节点是否准备好
     * 
     * 若均为 {@link #PRECOMMIT} 则返回 {@code true} 若存在 {@link #ABORT} 则返回 {@code false}
     * 
     * 若不满足上述两种情况，则一直阻塞
     * 
     * @param path
     * @return
     * @throws TransactionException
     */
    boolean check(String path) throws TransactionException;

    /**
     * 提交 修改原子服务上的 事务ID 的节点值修改为 {@link #COMMIT}
     * 
     * @param transactionId
     * @throws TransactionException
     */
    default void commit(long transactionId) throws TransactionException {
        commit(String.valueOf(transactionId));
    }

    /**
     * 提交 修改原子服务上的 事务ID 的临时子节点值修改为 {@link #COMMIT}
     * 
     * @param transactionId
     * @param serialId
     * @throws TransactionException
     */
    default void commit(long transactionId, int serialId) throws TransactionException {
        commit(transactionId + "/" + serialId);
    }

    /**
     * 提交 修改原子服务上的 {@code subPath} 的节点值修改为 {@link #DONE}
     * 
     * @param subPath
     * @throws TransactionException
     */
    void commit(String subPath) throws TransactionException;

}