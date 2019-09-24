package conglin.clrpc.service.future;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.exception.RpcServiceException;

public abstract class CompositeFuture extends RpcFuture {

    protected final List<RpcFuture> futures; 

    public CompositeFuture() {
        super();
        this.futures = new LinkedList<>();
    }

    public CompositeFuture(Collection<? extends RpcFuture> futures){
        super();
        this.futures = new LinkedList<>(futures);
    }

    /**
     * 将future添加进来
     * @param future
     * @return
     */
    public CompositeFuture addFuture(RpcFuture future){
        futures.add(future);
        return this;
    }

    @Override
    public void retry() {
        futures.parallelStream()
                .filter(RpcFuture::isPending)
                .forEach(RpcFuture::retry);
    }

    @Override
    public long identifier() {
        return futures.get(0).identifier();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException, RpcServiceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, RpcServiceException {
        throw new UnsupportedOperationException();
    }

    @Override
    public void done(Object result) {
        throw new UnsupportedOperationException();
    }

    @Override
    protected void doRunCallback() {
        futures.parallelStream().forEach(RpcFuture::runCallback);
        // if(!isError()){
        //     callback.success(response.getResult());
        // }else{
        //     callback.fail(remoteAddress, (RpcServiceException)response.getResult());
        // }
    }

}