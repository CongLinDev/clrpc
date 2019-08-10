package conglin.clrpc.common.util.concurrent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.exception.ResponseException;
import conglin.clrpc.service.AbstractServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;


public class RpcFuture implements Future<Object> {

    private static final Logger log = LoggerFactory.getLogger(RpcFuture.class);

    private final FutureSynchronizer synchronizer;

    private final BasicRequest request;
    private BasicResponse response;
    private String remoteAddress;

    private long startTime;
    private static final long timeThreshold = ConfigParser.getOrDefault("service.session.time-threshold", 5000);

    private Callback futureCallback;

    private static AbstractServiceHandler serviceHandler;

    public RpcFuture(BasicRequest request){
        this.request = request;
        this.synchronizer = new FutureSynchronizer();
        this.startTime = System.currentTimeMillis();
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

    /**
     * 收到回复信息后
     * 调用回调函数
     * 判断回复时间是否超过阈值
     * @param response
     */
    public void done(BasicResponse response){
        this.response = response;
        synchronizer.release(1);

        runCallback(futureCallback);
        
        if(response == null || response.isError() == true){
            log.error(response.getError());
            return;
        }

        long responseTime = futureTime();
        if(responseTime > timeThreshold){
            log.warn("Service response time is too slow. Request ID = "
                 + response.getRequestId()
                 + " Response Time(ms) = "
                 + responseTime);
        }
    }

    /**
     * 返回该 Future 从创建到调用该函数所经历的时间
     * 单位为 ms
     * @return
     */
    public long futureTime(){
        return System.currentTimeMillis() - startTime;
    }

    /**
     * 重置开始时间
     */
    public void resetTime(){
        this.startTime = System.currentTimeMillis();
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
     * 添加回调函数
     * 后添加的回调函数会覆盖前添加的回调函数
     * @param callback
     */
    public void addCallback(Callback callback){
        if(callback == null) return;
        if(isDone()){
            runCallback(callback);
        }else{
            futureCallback = callback;
        }
    }

    /**
     * 注册到一个 {@link AbstractServiceHandler} 上
     * {@link RpcFuture#runCallback(Callback)} 中的
     * 回调函数将提交到这个线程池中
     * 
     * @param serviceHandler
     */
    public static void registerThreadPool(AbstractServiceHandler serviceHandler){
        RpcFuture.serviceHandler = serviceHandler;
    }

    // public void future

    /**
     * 若存在有注册的 {@link ServiceHandler} ，则使用此线程池
     * 反之使用当前线程顺序执行
     * @param callback
     */
    private void runCallback(Callback callback){
        if(callback == null) return;
        
        if(serviceHandler != null){
            serviceHandler.submit(() -> runCallbackCore(callback));
        }else{
            runCallbackCore(callback);
        }
    }

    /**
     * 运行回调函数
     * @param callback
     */
    private void runCallbackCore(Callback callback){
        if(!response.isError()){
            callback.success(response.getResult());
        }else{
            callback.fail(remoteAddress, new ResponseException(response));
        }
    }

    /**
     * 用于RpcFuture的同步器
     */
    class FutureSynchronizer extends AbstractQueuedSynchronizer{
    
        private static final long serialVersionUID = -3359796046494665489L;
        
        private final int DONE = 1;// 完成
        private final int PENDING = 0;//等待
        //private final int CANCEL = -1;//取消
    
        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == DONE;
        }
    
        @Override
        protected boolean tryRelease(int arg) {
            if(getState() == PENDING){
               return compareAndSetState(PENDING, DONE);
            }else{
                return true;
            }
        }
    
        /**
         * 是否完成
         * @return
         */
        public boolean isDone(){
            return getState() == DONE;
        }
    }

}