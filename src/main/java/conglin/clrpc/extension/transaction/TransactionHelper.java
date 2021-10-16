package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.Callback;

public interface TransactionHelper {



    /**
     * 开始事务
     *
     * 该方法由事务协调者调用
     * 
     * @param transactionId 事务id
     * @throws TransactionException 失败抛出
     */
    default void begin(long transactionId) throws TransactionException {
        begin(String.valueOf(transactionId));
    }

    /**
     * 开始事务
     *
     * 该方法由事务协调者调用
     * 
     * @param path 路径
     * @throws TransactionException 失败抛出
     */
    void begin(String path) throws TransactionException;

    /**
     * 创建原子命令
     *
     * 该方法由事务协调者调用
     * 
     * @param transactionId 事务id
     * @param serialId      序列id
     * @param target        指定的参与者信息
     * @throws TransactionException 失败抛出
     */
    default void prepare(long transactionId, int serialId, String target) throws TransactionException {
        prepare(transactionId + "/" + serialId, target);
    }

    /**
     * 创建原子命令
     *
     * 该方法由事务协调者调用
     *
     * @param path 路径
     * @param target 指定的参与者信息
     * @throws TransactionException 失败抛出
     */
    void prepare(String path, String target) throws TransactionException;

    /**
     * 事务参与者查看是否占有执行原子命令的权限
     *
     * 该方法由事务参与者调用
     *
     * @param transactionId 事务id
     * @param serialId      序列id
     * @param target        当前参与者信息
     * @return 是否占有
     * @throws TransactionException 失败抛出
     */
    default boolean isOccupied(long transactionId, int serialId, String target) throws TransactionException {
        return isOccupied(transactionId + "/" + serialId, target);
    }

    /**
     * 事务参与者查看是否占有执行原子命令的权限
     *
     * 该方法由事务参与者调用
     *
     * @param path   路径
     * @param target 当前参与者信息
     * @return 是否占有
     * @throws TransactionException 失败抛出
     */
    boolean isOccupied(String path, String target) throws TransactionException;

    /**
     * 标记原子命令预提交成功
     *
     * 该方法由事务参与者调用
     *
     * @param transactionId 事务id
     * @param serialId      序列id
     * @param target        当前参与者信息
     * @return 是否标记成功
     * @throws TransactionException 失败抛出
     */
    default boolean signSuccess(long transactionId, int serialId, String target) throws TransactionException {
        return signSuccess(transactionId + "/" + serialId, target);
    }

    /**
     * 标记原子命令预提交成功
     *
     * 该方法由事务参与者调用
     *
     * @param path   路径
     * @param target 当前参与者信息
     * @return 是否标记成功
     * @throws TransactionException 失败抛出
     */
    boolean signSuccess(String path, String target) throws TransactionException;

    /**
     * 标记原子命令预提交失败
     *
     * 该方法由事务参与者调用
     *
     * @param transactionId 事务id
     * @param serialId      序列id
     * @param target        当前参与者信息
     * @return 是否标记成功
     * @throws TransactionException 失败抛出
     */
    default boolean signFailed(long transactionId, int serialId, String target) throws TransactionException {
        return signFailed(transactionId + "/" + serialId, target);
    }

    /**
     * 标记原子命令预提交失败
     *
     * 该方法由事务参与者调用
     *
     * @param path          路径
     * @param target        当前参与者信息
     * @return 是否标记成功
     * @throws TransactionException 失败抛出
     */
    boolean signFailed(String path, String target) throws TransactionException;

    /**
     * 监视事务状态
     *
     * 如果事务提交，执行 {@link Callback#success(Object)} 方法
     * 如果事务回滚则执行 {@link Callback#fail(Exception)} 方法
     *
     * 该方法由事务参与者调用
     *
     * @param transactionId 事务id
     * @param serialId      序列id
     * @param callback      回调对象
     * @throws TransactionException 失败抛出
     */
    default void watch(long transactionId, int serialId, Callback callback) throws TransactionException {
        watch(transactionId + "/" + serialId, callback);
    }

    /**
     * 监视事务状态
     *
     * 如果事务提交，执行 {@link Callback#success(Object)} 方法
     * 如果事务回滚则执行 {@link Callback#fail(Exception)} 方法
     *
     * 该方法由事务参与者调用
     * 
     * @param path  路径
     * @param callback 回调对象
     * @throws TransactionException 失败抛出
     */
    void watch(String path, Callback callback) throws TransactionException;

    /**
     * 中止事务
     *
     * 该方法由事务协调者调用
     *
     * @param transactionId 事务id
     * @throws TransactionException 失败抛出
     */
    default void abort(long transactionId) throws TransactionException {
        abort(String.valueOf(transactionId));
    }

    /**
     * 中止事务
     *
     * 该方法由事务协调者调用
     *
     * @param path                  路径
     * @throws TransactionException 失败抛出
     */
    void abort(String path) throws TransactionException;

    /**
     * 提交事务
     *
     * 该方法由事务协调者调用
     *
     * @param transactionId 事务id
     * @throws TransactionException 失败抛出
     */
    default void commit(long transactionId) throws TransactionException {
        commit(String.valueOf(transactionId));
    }

    /**
     * 提交事务
     *
     * 该方法由事务协调者调用
     *
     * @param path                  路径
     * @throws TransactionException 失败抛出
     */
    void commit(String path) throws TransactionException;
}
