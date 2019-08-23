package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.transfer.sender.RequestSender;

abstract public class RpcFuture implements Future<Object> {
    protected final FutureSynchronizer synchronizer;

    protected static ExecutorService executorService;
    protected Callback futureCallback;

    protected static final long TIME_THRESHOLD = ConfigParser.getOrDefault("service.session.time-threshold", 5000);

    protected long startTime; // 开始时间

    protected RequestSender sender;

    public RpcFuture(RequestSender sender){
        this.sender = sender;
        this.synchronizer = new FutureSynchronizer();
        startTime = System.currentTimeMillis();
    }

    
    /**
     * 重试
     */
    abstract public void retry();

    abstract public long identifier();

    @Override
    abstract public boolean cancel(boolean mayInterruptIfRunning);

    @Override
    abstract public boolean isCancelled();

    @Override
    abstract public boolean isDone();

    @Override
    abstract public Object get() throws InterruptedException, ExecutionException;

    @Override
    abstract public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException;

    /**
     * 收到回复信息后
     * 调用回调函数
     * @param result
     */
    abstract public void done(Object result);

    /**
     * 返回该future是否超时
     * @return
     */
    public boolean timeout(){
        return TIME_THRESHOLD + startTime > System.currentTimeMillis();
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
     * 若存在有注册的 {@link ServiceHandler} ，则使用此线程池
     * 反之使用当前线程顺序执行
     * @param callback
     */
    protected void runCallback(Callback callback){
        if(callback == null) return;
        
        if(executorService != null){
            executorService.submit(() -> runCallbackCore(callback));
        }else{
            runCallbackCore(callback);
        }
    }

    /**
     * 回调函数具体实现函数
     * @param callback
     */
    abstract protected void runCallbackCore(Callback callback);

    /**
     * 重置开始时间
     */
    protected void resetTime(){
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 注册一个线程池
     * @param executorService
     */
    public static void registerThreadPool(ExecutorService executorService){
        RpcFuture.executorService = executorService;
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