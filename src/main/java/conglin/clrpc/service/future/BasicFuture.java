package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.sender.RequestSender;

public class BasicFuture extends RpcFuture {

    private final BasicRequest request;
    private BasicResponse response;
    private String remoteAddress;

    public BasicFuture(RequestSender sender, BasicRequest request){
        super(sender);
        this.request = request;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isDone() {
        return synchronizer.isDone();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        synchronizer.acquire(-1);
        return (response != null) ? response.getResult() : null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if(synchronizer.tryAcquireNanos(-1, unit.toNanos(timeout))){
            return (response != null) ? response.getResult() : null;
        }else{
            throw new TimeoutException("Timeout: " + request.toString());
        }
    }

    
    @Override
    public void done(Object result) {
        this.response = (BasicResponse)response;
        synchronizer.release(1);
        runCallback(futureCallback);
    }


    @Override
    public void retry() {
        sender.resendRequest(remoteAddress, request);
        resetTime();
    }

    @Override
    public long identifier() {
        return request.getRequestId();
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicRequest
     * @return
     */
    public BasicRequest getRequest(){
        return this.request;
    }

    /**
     * 获取该 RpcFuture 相关的远端地址
     * 即 请求发送的目的地地址
     * @return
     */
    public String getRemoteAddress(){
        return this.remoteAddress;
    }

    /**
     * 设置该 RpcFuture 相关的远端地址
     * 即 请求发送的目的地地址
     * @param addr
     */
    public void setRemoteAddress(String addr){
        this.remoteAddress = addr;
    }

    /**
     * 运行回调函数
     * @param callback
     */
    @Override
    protected void runCallbackCore(Callback callback){
        if(!response.isError()){
            callback.success(response.getResult());
        }else{
            callback.fail(remoteAddress, (RpcServiceException)response.getResult());
        }
    }
}