package conglin.clrpc.service.future;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.transfer.message.TransactionRequest;
import conglin.clrpc.transfer.sender.RequestSender;

public class TransactionFuture extends RpcFuture {

    protected final List<TransactionRequest> requests;
    protected ZooKeeperTransactionHelper helper;

    public TransactionFuture(RequestSender sender, List<TransactionRequest> requests, ZooKeeperTransactionHelper helper) {
        super(sender);
        this.requests = requests;
    }

    @Override
    public void retry() {
        // TODO Auto-generated method stub
    }

    @Override
    public long identifier() {
        return requests.get(0).getRequestId();
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
    protected void doRunCallback(Callback callback) {
        // TODO Auto-generated method stub

    }

}