package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;

abstract public class AbstractFuture implements RpcFuture {

    private final FutureSynchronizer SYNCHRONIZER; // 同步器

    protected Callback futureCallback; // 回调

    private long startTime; // 开始时间
    private boolean error; // 是否出错，只有在该future已经完成的情况下，该变量才有效
    private boolean fallback; // 是否是 fallback产生的结果，只有在该future已经完成的情况下，该变量才有效

    public AbstractFuture() {
        this.SYNCHRONIZER = new FutureSynchronizer();
        startTime = System.currentTimeMillis();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        try {
            SYNCHRONIZER.acquire(0);
            return doGet();
        } finally {
            SYNCHRONIZER.release(0);
        }
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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
     * @throws RpcServiceException
     */
    abstract protected Object doGet() throws RpcServiceException;

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
        if(SYNCHRONIZER.cancel()) {
            SYNCHRONIZER.release(0);
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
    public boolean isFallback() {
        return fallback;
    }

    @Override
    public void signFallback() {
        fallback = true;
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
    public boolean addCallback(Callback callback) {
        if (futureCallback != null)
            return false;
        this.futureCallback = callback;
        if (isDone())
            runCallback();
        return true;
    }

    /**
     * 执行回调函数
     */
    protected void runCallback() {
        if (!isCancelled() && futureCallback != null)
            doRunCallback();
    }

    /**
     * 回调函数具体实现函数
     */
    abstract protected void doRunCallback();

    /**
     * 重置开始时间
     */
    private void resetTime() {
        this.startTime = System.currentTimeMillis();
    }

    /**
     * 用于RpcFuture的同步器
     */
    static class FutureSynchronizer extends AbstractQueuedSynchronizer {

        private static final long serialVersionUID = -3359796046494665489L;
        // if pending, retry times == Math.abs(getState())
        private static final int PENDING = 0; // 等待
        private static final int CANCELLED = 1; // 取消
        private static final int DONE = 2; // 完成
        private static final int USED = 3; // 占用

        @Override
        protected boolean tryAcquire(int arg) {
            if (isCancelled())
                return true;
            return compareAndSetState(DONE, USED);
        }

        @Override
        protected boolean tryRelease(int arg) {               
            if (!isCancelled()) {
                 setState(DONE);
            }
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
            return getState() < CANCELLED;
        }

        /**
         * 取消
         */
        protected boolean cancel() {
            int curState = getState();
            return curState < DONE && compareAndSetState(curState, CANCELLED);
        }

        /**
         * 重试
         * 
         * @return 是否重试成功
         */
        protected boolean retry() {
            int curState = getState();
            if (curState > PENDING)
                return false;
            return compareAndSetState(curState, curState - 1);
        }

        /**
         * 已经重试的次数
         * 
         * @return
         */
        protected int retryTimes() {
            return Math.max(-getState(), 0);
        }
    }
}