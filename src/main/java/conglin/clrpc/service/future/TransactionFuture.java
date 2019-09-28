package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;

public class TransactionFuture extends AbstractCompositeFuture {

    protected ZooKeeperTransactionHelper helper;

    public TransactionFuture(ZooKeeperTransactionHelper helper) {
        super();
        this.helper = helper;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException, RpcServiceException {
        try{
            synchronizer.acquire(0);
            return null;
        }finally{
            synchronizer.release(0);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, RpcServiceException {
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
        // 完成后 helper 可能会被其他的 Future 占用， 故在此释放防止干扰其他 Future
        helper = null;
    }

    /**
     * 因为事务类型的Future无具体的结果，所以
     * {@link TransactionFuture#doRunCallback()} 
     * 调用的 {@link Callback#success(Object)} 以及 
     * {@link Callback#fail(String, RpcServiceException)} 参数均为 {@code null}
     */
    @Override
    protected void doRunCallback() {
        if(!isError()){
            this.futureCallback.success(null);
        }else{
            this.futureCallback.fail(null, null);
        }
    }


    @Override
    public AbstractCompositeFuture add(RpcFuture future) {
        super.add(future);
        helper.call(future.identifier(), futures.size());
        return this;
    }


    public AbstractCompositeFuture submit(){
        // TODO submit
        return this;
    }

    public AbstractCompositeFuture rollback(){
        // TODO rollback
        return this;
    }

    public AbstractCompositeFuture begin(){
        // TODO begin
        return this;
    }

}