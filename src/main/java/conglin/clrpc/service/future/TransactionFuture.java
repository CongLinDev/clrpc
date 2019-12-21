package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.FutureCancelledException;
import conglin.clrpc.common.exception.RequestException;

public class TransactionFuture extends AbstractCompositeFuture {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionFuture.class);

    protected final Callback subFutureCallback;

    public TransactionFuture() {
        super();

        subFutureCallback = new Callback() {
            @Override
            public void success(Object result) {
                // 进入该方法的时候，说明事务已经提交，不会被取消或中止
                try {
                    if (checkCompleteFuture()) {
                        LOGGER.info("Transaction request id=" + identifier() + " commit successfully.");
                        done(null); // 全部的子Future完成后调用组合Future完成
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
                // 该方法一定不会被调用
                // 因为当事务Future接收到原子请求的回复时候， 原子请求一定执行完成了
                // 若原子请求执行失败，则请求会被服务提供者抛弃而不返回任何回复
                setError();
                LOGGER.error(e.getMessage());
            }
        };
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException, RequestException {
        try {
            SYNCHRONIZER.acquire(0);
            return null;
        } finally {
            SYNCHRONIZER.release(0);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, RequestException {
        try {
            if (SYNCHRONIZER.tryAcquireNanos(0, unit.toNanos(timeout))) {
                return null;
            } else {
                throw new TimeoutException("TransactionFuture Timeout! " + identifier());
            }
        } finally {
            SYNCHRONIZER.release(0);
        }
    }

    @Override
    public void done(Object result) {
        SYNCHRONIZER.release(0);
        runCallback();
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