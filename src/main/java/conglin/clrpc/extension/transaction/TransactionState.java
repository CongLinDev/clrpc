package conglin.clrpc.extension.transaction;

/**
 * 事务状态
 */
public enum TransactionState {
    PREPARE, // 准备
    PRECOMMIT, // 预提交
    COMMIT, // 提交
    ABORT // 中止
}