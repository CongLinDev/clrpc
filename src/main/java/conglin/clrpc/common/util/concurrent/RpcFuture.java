package conglin.clrpc.common.util.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.exception.ResponseException;
import conglin.clrpc.service.AbstractServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;


public class RpcFuture implements Future<Object> {

    private static final Logger log = LoggerFactory.getLogger(RpcFuture.class);

    private SimpleSynchronizer synchronizer;

    private BasicRequest request;
    private BasicResponse response;

    private long startTime;
    private static long timeThreshold = ConfigParser.getInstance().getOrDefault("service.session.time-threshold", 5000);

    private List<Callback> callbacks;

    private ReentrantLock lock;

    private static AbstractServiceHandler serviceHandler;

    public RpcFuture(BasicRequest request){
        this.request = request;
        this.synchronizer = new SimpleSynchronizer();
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

        invokeCallbacks();
        
        if(response == null || response.isError() == true){
            log.error(response.getError());
            return;
        }

        long responseTime = System.currentTimeMillis() - startTime;
        if(responseTime > timeThreshold){
            log.warn("Service response time is too slow. Request ID = "
                 + response.getRequestId()
                 + " Response Time(ms) = "
                 + responseTime);
        }
    }

    /**
     * 添加回调函数
     * 选择在此创建回调函数集合以及和可重入锁的原因是
     * 有些 {@link RpcFuture} 并没有回调函数
     * @param callback
     * @return
     */
    public RpcFuture addCallback(Callback callback){
        createCallbackLock();
        lock.lock();

        try{
            if(isDone()){
                runCallback(callback);
            }else{
                callbacks.add(callback);
            }
        }finally{
            lock.unlock();
        }
        return this;
    }

    /**
     * 创建回调函数集合以及和可重入锁
     */
    private void createCallbackLock(){
        if(lock == null){
            synchronized(this){
                if(lock == null){
                    lock = new ReentrantLock();
                    callbacks = new ArrayList<>();
                }
            }
        }
    }

    /**
     * 回调所有回调函数
     */
    private void invokeCallbacks(){
        if(callbacks != null){
            lock.lock();
            try{
                callbacks.forEach((callback)-> runCallback(callback));
            }finally{
                lock.unlock();
            }
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

    /**
     * 若存在有注册的 {@link ServiceHandler} ，则使用此线程池
     * 反之使用当前线程顺序执行
     * @param callback
     */
    private void runCallback(Callback callback){
        // final BasicResponse res = this.response;
        if(serviceHandler != null){
            serviceHandler.submit(() -> runCallbackCore(callback));
        }else{
            runCallbackCore(callback);
        }
    }

    private void runCallbackCore(Callback callback){
        if(!response.isError()){
            callback.success(response.getResult());
        }else{
            callback.fail(new ResponseException(response));
        }
    }

}