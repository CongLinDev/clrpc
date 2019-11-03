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
    
    private static final Logger log = LoggerFactory.getLogger(TransactionFuture.class);

    protected final Callback subFutureCallback;

    public TransactionFuture() {
        super();

        subFutureCallback = new Callback() {
            @Override
            public void success(Object result) {
                try {
                    if(checkCompleteFuture())
                        done(null);
                } catch (FutureCancelledException e) {
                    cancel(true);
                    log.error(e.getMessage());
                    // rollback...
                    
                }
            }

            @Override
            public void fail(Exception e) {
                setError();
                log.error(e.getMessage());
                // rollback...

            }
        };
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException, RequestException {
        try{
            synchronizer.acquire(0);
            return null;
        }finally{
            synchronizer.release(0);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, RequestException {
        try{
            if(synchronizer.tryAcquireNanos(0, unit.toNanos(timeout))){
                return null;
            }else{
                throw new TimeoutException("TransactionFuture Timeout! " + identifier());
            }
        }finally{
            synchronizer.release(0);
        }
    }

    @Override
    public void done(Object result) {
        synchronizer.release(0);
        runCallback();
    }

    @Override
    protected void beforeCombine(RpcFuture future) {
        future.addCallback(subFutureCallback);
    }

    /**
     * 因为事务类型的Future无具体的结果，所以
     * {@link TransactionFuture#doRunCallback()} 
     * 调用的 {@link Callback#success(Object)} 以及 
     * {@link Callback#fail(String, RequestException)} 参数均为 {@code null}
     */
    @Override
    protected void doRunCallback() {
        if(!isError()){
            this.futureCallback.success(null);
        }else{
            this.futureCallback.fail(null);
        }
    }


}