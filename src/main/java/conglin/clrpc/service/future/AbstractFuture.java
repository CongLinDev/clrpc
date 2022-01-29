package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.future.sync.SignalStateSync;
import conglin.clrpc.service.future.sync.StateSync;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

abstract public class AbstractFuture implements RpcFuture {

    private final StateSync SYNCHRONIZER; // 同步器

    private Callback futureCallback; // 回调
    private FailStrategy failStrategy; // 失败策略
    private boolean error; // 是否出错，只有在该future已经完成的情况下，该变量才有效

    public AbstractFuture() {
        this.SYNCHRONIZER = newSynchronizer();
    }

    /**
     * 获取一个状态同步器
     * 
     * @return
     */
    protected StateSync newSynchronizer() {
        return new SignalStateSync();
    }

    @Override
    public Object get() throws InterruptedException, ExecutionException {
        SYNCHRONIZER.await();
        return doGet();
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

    /**
     * 设置错误标志位
     */
    protected void signError() {
        this.error = true;
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

    @Override
    public RpcFuture failStrategy(Class<? extends FailStrategy> strategyClass) {
        this.failStrategy = ClassUtils.loadObjectByParamType(strategyClass, FailStrategy.class,
                new Class<?>[] { RpcFuture.class }, new Object[] { this });
        return this;
    }

    @Override
    public FailStrategy failStrategy() {
        return this.failStrategy;
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
}