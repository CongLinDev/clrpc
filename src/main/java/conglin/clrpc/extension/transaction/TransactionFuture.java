package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.ServiceException;
import conglin.clrpc.service.future.AbstractCompositeFuture;
import conglin.clrpc.service.future.InvocationFuture;

public class TransactionFuture extends AbstractCompositeFuture {
    protected final Callback subFutureCallback;

    /**
     * 构造一个事务Future
     * 
     * @param transactionId 事务ID
     */
    public TransactionFuture() {
        super();

        subFutureCallback = new Callback() {
            @Override
            public void success(Object result) {
                // 进入该方法的时候，说明事务已经提交，不会被取消或中止
                if (!TransactionFuture.this.isError() && checkCompleteFuture()) {
                    TransactionFuture.this.done(false, null); // 全部的子Future完成后调用组合Future完成
                }
            }

            @Override
            public void fail(Exception e) {
                // 该方法只有当中止事务时，才会被执行
                // 而当原子请求执行错误时，不会向服务消费者发送回复
                TransactionFuture.this.signError();
                if (checkCompleteFuture()) {
                    TransactionFuture.this.done(false, null); // 全部的子Future完成后调用组合Future完成
                }
            }
        };
    }

    @Override
    protected void beforeCombine(InvocationFuture future) {
        future.callback(subFutureCallback);
    }

    @Override
    protected void doRunCallback(Callback callback) {
        if (!isError()) {
            callback.success(doGet());
        } else {
            callback.fail(new ServiceException("Transaction has been cancelled."));
        }
    }

}