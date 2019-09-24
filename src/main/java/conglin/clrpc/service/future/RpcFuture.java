package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.common.util.ConfigParser;

abstract public class RpcFuture implements Future<Object> {
    protected final FutureSynchronizer synchronizer;

    protected static ExecutorService executorService;
    protected Callback futureCallback;

    protected static final long TIME_THRESHOLD = ConfigParser.getOrDefault("service.session.time-threshold", 5000);

    protected long startTime; // 开始时间

    protected volatile boolean error; // 是否出错，只有在该future已经完成的情况下，该变量才有效

    public RpcFuture(){
        this.synchronizer = new FutureSynchronizer();
        startTime = System.currentTimeMillis();
    }
    
    /**
     * 重试
     */
    abstract public void retry();

    /**
     * 返回该 Future 的标识符
     * @return
     */
    abstract public long identifier();

    @Override
    abstract public boolean cancel(boolean mayInterruptIfRunning);

    @Override
    abstract public Object get() 
        throws InterruptedException, ExecutionException, RpcServiceException;

    @Override
    abstract public Object get(long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException, RpcServiceException;

    /**
     * 收到回复信息后
     * 调用回调函数
     * @param result
     */
    abstract public void done(Object result);

    @Override
    public boolean isCancelled() {
        return synchronizer.isCancelled();
    }

    @Override
    public boolean isDone() {
        return synchronizer.isDone();
    }

    /**
     * 是否正在等待中
     * @return
     */
    public boolean isPending(){
        return synchronizer.isPending();
    }


    /**
     * 该{@link RpcFuture} 是否出错
     * 只有在{@link RpcFuture#isDone()} 返回值为 true 的情况下
     * 该方法的返回值才可信
     * @return
     */
    public boolean isError(){
        return error;
    }

    /**
     * 设置错误标志位
     */
    protected void setError(){
        this.error = true;
    }

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
        this.futureCallback = callback;
        if(isDone()){
            runCallback();
        }
    }

    /**
     * 若存在有注册的 {@link ServiceHandler} ，则使用此线程池
     * 反之使用当前线程顺序执行
     * @param callback
     */
    protected void runCallback(){
        if(isCancelled()) return;
        
        if(executorService != null){
            executorService.submit(this::doRunCallback);
        }else{
            doRunCallback();
        }
    }

    /**
     * 回调函数具体实现函数
     */
    abstract protected void doRunCallback();

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

        private final int CANCELLED = -1;  // 取消
        private final int PENDING = 0;  // 等待
        private final int DONE = 1;     // 完成
        private final int USED = 2;     // 占用
    
        @Override
        protected boolean tryAcquire(int arg) {
            if(isCancelled()) return true;
            return compareAndSetState(DONE, USED);
        }
    
        @Override
        protected boolean tryRelease(int arg) {
            if(isUsed())
                return compareAndSetState(USED, DONE);
            if(isPending())
                return compareAndSetState(PENDING, DONE);
            return true;
        }
    
        /**
         * 是否完成
         * @return
         */
        public boolean isDone(){
            return getState() >= DONE;
        }

        /**
         * 是否取消
         * @return
         */
        public boolean isCancelled(){
            return getState() == CANCELLED;
        }

        /**
         * 是否被占用
         * @return
         */
        public boolean isUsed(){
            return getState() == USED;
        }

        /**
         * 是否等待中
         * @return
         */
        public boolean isPending(){
            return getState() == PENDING;
        }

        /**
         * 取消
         */
        public void cancel(){
            setState(CANCELLED);
        }

        /**
         * 重试
         */
        public void retry(){
            setState(PENDING);
        }
    }
}