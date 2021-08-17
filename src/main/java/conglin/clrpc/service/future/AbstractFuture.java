package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.Fallback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.service.future.sync.BasicStateSync;
import conglin.clrpc.service.future.sync.StateSync;

abstract public class AbstractFuture implements RpcFuture {

    private final StateSync SYNCHRONIZER; // 同步器

    private Callback futureCallback; // 回调
    private Fallback futureFallback;

    private long startTime; // 开始时间
    private boolean error; // 是否出错，只有在该future已经完成的情况下，该变量才有效

    public AbstractFuture() {
        this.SYNCHRONIZER = newSynchronizer();
        startTime = System.currentTimeMillis();
    }

    /**
     * 获取一个状态同步器
     * 
     * @return
     */
    protected StateSync newSynchronizer() {
        return new BasicStateSync();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            SYNCHRONIZER.await();
            return doGet();
        } finally {
            SYNCHRONIZER.signal();
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        try {
            if (SYNCHRONIZER.await(timeout, unit)) {
                return doGet();
            } else {
                throw new TimeoutException();
            }
        } finally {
            SYNCHRONIZER.signal();
        }
    }

    /**
     * 获取结果实际方法
     * 
     * @return
     * @throws RpcServiceException
     */
    abstract protected Object doGet() throws RpcServiceException;

    @Override
    public void done(Object result) {
        beforeDone(result);
        SYNCHRONIZER.signal();
        runCallback();
    }

    /**
     * 完成前一刻需要做的工作
     * 
     * @param result
     */
    protected void beforeDone(Object result) {
        // 默认不做任何事情
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (SYNCHRONIZER.cancel()) {
            SYNCHRONIZER.signal();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean isCancelled() {
        return SYNCHRONIZER.isCancelled();
    }

    @Override
    public boolean isDone() {
        return SYNCHRONIZER.isDone();
    }

    @Override
    public boolean isPending() {
        return SYNCHRONIZER.isPending();
    }

    @Override
    public boolean isError() {
        return error;
    }

    @Override
    public boolean retry() {
        if (SYNCHRONIZER.retry()) {
            resetTime();
            return true;
        }
        return false;
    }

    @Override
    public int retryTimes() {
        return SYNCHRONIZER.retryTimes();
    }

    /**
     * 设置错误标志位
     */
    protected void signError() {
        this.error = true;
    }

    @Override
    public boolean timeout(long timeThreshold) {
        return timeThreshold + startTime > System.currentTimeMillis();
    }


    @Override
    public Fallback fallback() {
        return futureFallback;
    }

    @Override
    public RpcFuture fallback(Fallback fallback) {
        this.futureFallback = fallback;
        return this;
    }

    @Override
    public RpcFuture callback(Callback callback) {
        if (isDone()) {
            runCallback(callback);
        } else {
            futureCallback = (futureCallback == null) ? callback : futureCallback.andThen(callback);
        }
        return this;
    }

    protected void runCallback(Callback callback) {
        if (callback != null)
            doRunCallback(callback);
    }

    /**
     * 执行回调函数
     */
    protected void runCallback() {
        runCallback(this.futureCallback);
    }

    /**
     * 回调函数具体实现方法
     * 
     * @param callback
     */
    abstract protected void doRunCallback(Callback callback);

    /**
     * 重置开始时间
     */
    private void resetTime() {
        this.startTime = System.currentTimeMillis();
    }
}