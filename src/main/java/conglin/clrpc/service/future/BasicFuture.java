package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.sender.RequestSender;

public class BasicFuture extends RpcFuture {

    private final BasicRequest request;
    private BasicResponse response;
    private String remoteAddress;

    protected RequestSender sender;

    public BasicFuture(RequestSender sender, BasicRequest request){
        super();
        this.request = request;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean canCancel = !synchronizer.isDone();
        synchronizer.cancel();
        return canCancel;
    }

    @Override
    public Object get()
        throws InterruptedException, ExecutionException, RpcServiceException {
        try{
            synchronizer.acquire(0);
            if(response == null) return null;
            if(response.isError()){
                setError();
                throw (RpcServiceException)response.getResult();
            }
            return response.getResult();
        }finally{
            synchronizer.release(0);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException, RpcServiceException {
        try{
            if(synchronizer.tryAcquireNanos(0, unit.toNanos(timeout))){
                if(response == null) return null;
                if(response.isError()){
                    setError();
                    throw (RpcServiceException)response.getResult();
                }
                return response.getResult();
            }else{
                throw new TimeoutException("Timeout: " + request.toString());
            }
        }finally{
            synchronizer.release(0);
        }
    }

    @Override
    public void done(Object result) {
        this.response = (BasicResponse)result;
        synchronizer.release(0);
        runCallback();
    }

    @Override
    public void retry() {
        synchronizer.retry();
        resetTime();
        sender.resendRequest(remoteAddress, request);
    }

    @Override
    public long identifier() {
        return request.getRequestId();
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicRequest
     * @return
     */
    public BasicRequest request(){
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
    protected void doRunCallback(){
        if(!isError()){
            this.futureCallback.success(response.getResult());
        }else{
            this.futureCallback.fail(remoteAddress, (RpcServiceException)response.getResult());
        }
    }
}