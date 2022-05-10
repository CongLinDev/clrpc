package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.Callback;

 /**
  *  consumer                          provider
  *
  *   begin
  *     |
  *    \|/
  *     |
  *  prepare
  *     |
  *    \|/
  *     |
  * send request        ->          receive request
  * (not blocking)
  *                                         |
  *                                        \|/
  *                                         |
  *                                    isOccupied
  *                                         |
  *                                        \|/
  *                                         |
  *                            invoke method (do pre-commit action)
  *                                         |
  *                                        \|/
  *                                         |
  *                               signPrecommit or signAbort
  *                                         |
  *                                        \|/
  *                                         |
  *                              watch (wait transaction state)
  *                                         |
  *                                        \|/
  *                                         |
  * receive response     <-            send response
  *  (not blocking)
  *     |
  *    \|/
  *     |
  * commit or abort                         .
  *                                         .
  *                                         .
  *                              do commit or rollback action
  *                                         |
  *                                        \|/
  *                                         |
  *                               signCommit or signAbort
  */
public interface TransactionHelper {

    /**
     * 开始事务
     *
     * 该方法由事务协调者调用
     * 
     * @param transactionId 事务id
     * @param target 参与者信息
     * @throws TransactionException 失败抛出
     */
    default void begin(long transactionId, String target) throws TransactionException {
        begin(String.valueOf(transactionId), target);
    }

    /**
     * 开始事务
     *
     * 该方法由事务协调者调用
     * 
     * @param path 路径
     * @param target 参与者信息
     * @throws TransactionException 失败抛出
     */
    void begin(String path, String target) throws TransactionException;

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
     * @param path   路径
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
    default boolean signPrecommit(long transactionId, int serialId, String target) throws TransactionException {
        return signPrecommit(transactionId + "/" + serialId, target);
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
    boolean signPrecommit(String path, String target) throws TransactionException;

    /**
     * 标记原子命令提交成功
     *
     * 该方法由事务参与者调用
     *
     * @param transactionId 事务id
     * @param serialId      序列id
     * @param target        当前参与者信息
     * @return 是否标记成功
     * @throws TransactionException 失败抛出
     */
    default boolean signCommit(long transactionId, int serialId, String target) throws TransactionException {
        return signCommit(transactionId + "/" + serialId, target);
    }

    /**
     * 标记原子命令提交成功
     *
     * 该方法由事务参与者调用
     *
     * @param path   路径
     * @param target 当前参与者信息
     * @return 是否标记成功
     * @throws TransactionException 失败抛出
     */
    boolean signCommit(String path, String target) throws TransactionException;

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
    default boolean signAbort(long transactionId, int serialId, String target) throws TransactionException {
        return signAbort(transactionId + "/" + serialId, target);
    }

    /**
     * 标记原子命令预提交失败
     *
     * 该方法由事务参与者调用
     *
     * @param path   路径
     * @param target 当前参与者信息
     * @return 是否标记成功
     * @throws TransactionException 失败抛出
     */
    boolean signAbort(String path, String target) throws TransactionException;

    /**
     * 监视事务状态
     *
     * 如果事务提交，执行 {@link Callback#success(Object)} 方法
     * 如果事务回滚则执行 {@link Callback#fail(Exception)} 方法
     *
     * 该方法由事务参与者调用
     *
     * @param transactionId 事务id
     * @param callback      回调对象
     * @throws TransactionException 失败抛出
     */
    default void watch(long transactionId, Callback callback) throws TransactionException {
        watch(String.valueOf(transactionId), callback);
    }

    /**
     * 监视事务状态
     *
     * 如果事务提交，执行 {@link Callback#success(Object)} 方法
     * 如果事务回滚则执行 {@link Callback#fail(Exception)} 方法
     *
     * 该方法由事务参与者调用
     * 
     * @param path     路径
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
     * @param target 参与者信息
     * @throws TransactionException 失败抛出
     */
    default void abort(long transactionId, String target) throws TransactionException {
        abort(String.valueOf(transactionId), target);
    }

    /**
     * 中止事务
     *
     * 该方法由事务协调者调用
     *
     * @param path 路径
     * @param target
     * @throws TransactionException 失败抛出
     */
    void abort(String path, String target) throws TransactionException;

    /**
     * 提交事务
     *
     * 该方法由事务协调者调用
     *
     * @param transactionId 事务id
     * @param target 参与者信息
     * @throws TransactionException 失败抛出
     */
    default void commit(long transactionId, String target) throws TransactionException {
        commit(String.valueOf(transactionId), target);
    }

    /**
     * 提交事务
     *
     * 该方法由事务协调者调用
     *
     * @param path 路径
     * @param target 参与者信息
     * @throws TransactionException 失败抛出
     */
    void commit(String path, String target) throws TransactionException;
}
