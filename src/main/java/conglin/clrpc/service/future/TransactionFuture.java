package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;

public class TransactionFuture extends CompositeFuture {

    protected ZooKeeperTransactionHelper helper;

    public TransactionFuture(ZooKeeperTransactionHelper helper) {
        super();
        this.helper = helper;
    }

    @Override
    public void retry() {
        // TODO Auto-generated method stub
    }

    @Override
    public long identifier() {
        return super.identifier();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException, RpcServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, RpcServiceException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void done(Object result) {
        // TODO Auto-generated method stub

        helper = null; // 完成后 helper 可能会被其他的 Future 占用， 故在此释放防止干扰其他 Future
    }

    @Override
    protected void doRunCallback() {
        super.doRunCallback();
    }

}