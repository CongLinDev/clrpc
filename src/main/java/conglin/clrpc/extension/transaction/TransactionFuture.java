package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.service.future.AbstractCompositeFuture;
import conglin.clrpc.service.future.RpcFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionFuture extends AbstractCompositeFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFuture.class);

    protected final Callback subFutureCallback;

    private final long transactionId;

    /**
     * 构造一个事务Future
     * 
     * @param transactionId 事务ID
     */
    public TransactionFuture(long transactionId) {
        super();
        this.transactionId = transactionId;

        subFutureCallback = new Callback() {
            @Override
            public void success(Object result) {
                // 进入该方法的时候，说明事务已经提交，不会被取消或中止
                if (!TransactionFuture.this.isError() && checkCompleteFuture()) {
                    LOGGER.debug("Transaction request id={} commit successfully.", identifier());
                    TransactionFuture.this.done(null); // 全部的子Future完成后调用组合Future完成
                }
            }

            @Override
            public void fail(Exception e) {
                // 该方法只有当中止事务时，才会被执行
                // 而当原子请求执行错误时，不会向服务消费者发送回复
                TransactionFuture.this.signError();
                if (checkCompleteFuture()) {
                    LOGGER.debug("Transaction request id={} abort successfully.", identifier());
                    TransactionFuture.this.done(null); // 全部的子Future完成后调用组合Future完成
                }
            }
        };
    }

    @Override
    public long identifier() {
        return transactionId;
    }

    @Override
    protected void beforeCombine(RpcFuture future) {
        future.callback(subFutureCallback);
    }

    @Override
    protected void doRunCallback(Callback callback) {
        if (!isError()) {
            callback.success(doGet());
        } else {
            callback.fail(new RpcServiceException("Transaction has been cancelled."));
        }
    }

}