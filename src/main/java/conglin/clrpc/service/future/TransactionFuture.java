package conglin.clrpc.service.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.FutureCancelledException;
import conglin.clrpc.common.exception.RequestException;

public class TransactionFuture extends AbstractCompositeFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFuture.class);

    protected final Callback subFutureCallback;

    private final long transactionId;

    public TransactionFuture(long transactionId) {
        super();
        this.transactionId = transactionId;

        subFutureCallback = new Callback() {
            @Override
            public void success(Object result) {
                // 进入该方法的时候，说明事务已经提交，不会被取消或中止
                try {
                    if (checkCompleteFuture()) {
                        LOGGER.debug("Transaction request id=" + identifier() + " commit successfully.");
                        TransactionFuture.this.done(null); // 全部的子Future完成后调用组合Future完成
                    }

                } catch (FutureCancelledException e) {
                    // 因为事务已经提交
                    // 某一个子操作取消后，中止不会成功
                    // cancel(true);
                    LOGGER.error(e.getMessage());
                }
            }

            @Override
            public void fail(Exception e) {
                // 该方法只有当中止事务时，才会被执行
                // 而当原子请求执行错误时，不会向服务消费者发送回复
                setError();
                LOGGER.error(e.getMessage());
            }
        };
    }

    @Override
    protected Object doGet() throws RequestException {
        // do nothing
        return null;
    }

    @Override
    public long identifier() {
        return transactionId;
    }

    @Override
    protected void beforeCombine(RpcFuture future) {
        future.addCallback(subFutureCallback);
    }

    /**
     * 因为事务类型的Future无具体的结果，所以 {@link TransactionFuture#doRunCallback()} 调用的
     * {@link Callback#success(Object)} 以及
     * {@link Callback#fail(String, RequestException)} 参数均为 {@code null}
     */
    @Override
    protected void doRunCallback() {
        if (!isError()) {
            this.futureCallback.success(null);
        } else {
            this.futureCallback.fail(null);
        }
    }

}