package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.FutureRepeatedCallbackException;
import conglin.clrpc.common.exception.RequestException;

abstract public class AbstractFuture implements RpcFuture {
    protected static long TIME_THRESHOLD = 5000;

    public static void setTimeThreshold(long timeThreshold) {
        TIME_THRESHOLD = timeThreshold;
    }

    private final FutureSynchronizer SYNCHRONIZER; // 同步器

    protected Callback futureCallback; // 回调

    protected long startTime; // 开始时间
    protected volatile boolean error; // 是否出错，只有在该future已经完成的情况下，该变量才有效

    public AbstractFuture() {
        this.SYNCHRONIZER = new FutureSynchronizer();
        startTime = System.currentTimeMillis();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException, RequestException {
        try {
            SYNCHRONIZER.acquire(0);
            return doGet();
        } finally {
            SYNCHRONIZER.release(0);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit)
            throws InterruptedException, ExecutionException, TimeoutException, RequestException {
        try {
            if (SYNCHRONIZER.tryAcquireNanos(0, unit.toNanos(timeout))) {
                return doGet();
            } else {
                throw new TimeoutException();
            }
        } finally {
            SYNCHRONIZER.release(0);
        }
    }

    /**
     * 获取结果实际方法
     * 
     * @return
     * @throws RequestException
     */
    abstract protected Object doGet() throws RequestException;

    @Override
    public void done(Object result) {
        beforeDone(result);
        SYNCHRONIZER.release(0);
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
        boolean canCancel = !SYNCHRONIZER.isDone();
        SYNCHRONIZER.cancel();
        return canCancel;
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
    public void retry() {
        SYNCHRONIZER.retry();
    }

    /**
     * 设置错误标志位
     */
    protected void setError() {
        this.error = true;
    }

    @Override
    public boolean timeout() {
        return TIME_THRESHOLD + startTime > System.currentTimeMillis();
    }

    @Override
    public void addCallback(Callback callback) {
        if (futureCallback == null)
            throw new FutureRepeatedCallbackException(futureCallback);
        this.futureCallback = callback;
        if (isDone())
            runCallback();
    }

    /**
     * 执行回调函数
     */
    protected void runCallback() {
        if (!isCancelled())
            doRunCallback();
    }

    /**
     * 回调函数具体实现函数
     */
    abstract protected void doRunCallback();

    /**
     * 重置开始时间
     */
    protected void resetTime() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 用于RpcFuture的同步器
     */
    class FutureSynchronizer extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = -3359796046494665489L;

        private static final int CANCELLED = -1; // 取消
        private static final int PENDING = 0; // 等待
        private static final int DONE = 1; // 完成
        private static final int USED = 2; // 占用

        @Override
        protected boolean tryAcquire(int arg) {
            if (isCancelled())
                return true;
            return compareAndSetState(DONE, USED);
        }

        @Override
        protected boolean tryRelease(int arg) {
            if (isUsed())
                return compareAndSetState(USED, DONE);
            if (isPending())
                return compareAndSetState(PENDING, DONE);
            return true;
        }

        /**
         * 是否完成
         * 
         * @return
         */
        protected boolean isDone() {
            return getState() >= DONE;
        }

        /**
         * 是否取消
         * 
         * @return
         */
        protected boolean isCancelled() {
            return getState() == CANCELLED;
        }

        /**
         * 是否被占用
         * 
         * @return
         */
        protected boolean isUsed() {
            return getState() == USED;
        }

        /**
         * 是否等待中
         * 
         * @return
         */
        protected boolean isPending() {
            return getState() == PENDING;
        }

        /**
         * 取消
         */
        protected void cancel() {
            setState(CANCELLED);
        }

        /**
         * 重试
         */
        protected void retry() {
            setState(PENDING);
        }
    }
}