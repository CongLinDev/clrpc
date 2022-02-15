package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;
import conglin.clrpc.service.future.strategy.FailStrategy;

import java.util.concurrent.Future;
import java.util.function.Consumer;

/**
 * future 对象 用于同步或异步地获取任务的执行结果
 * 
 * @see java.util.concurrent.Future
 */
public interface InvocationFuture extends Future<Object> {

    /**
     * 添加回调
     * 
     * @param callback
     * @return this
     */
    InvocationFuture callback(Callback callback);

    /**
     * 添加通用的回调
     * 
     * @param task
     * @return this
     */
    default InvocationFuture callback(Runnable task) {
        return callback(Callback.convert(task));
    }

    /**
     * 添加通用的回调
     * 
     * @param any
     * @return this
     */
    default InvocationFuture callback(Consumer<Object> any) {
        return callback(Callback.convert(any));
    }

    /**
     * 该 {@link InvocationFuture} 的标识符
     * 
     * @return
     */
    long identifier();

    /**
     * 确认该 {@link InvocationFuture} 完成
     * 
     * @param result
     */
    void done(Object result);

    /**
     * 是否等待中
     * 
     * @return
     */
    boolean isPending();

    /**
     * 该 {@link InvocationFuture} 是否出错
     * 
     * 只有在{@link InvocationFuture#isDone()} 返回值为 {@code true} 的情况下 该方法的返回值才可信
     * 
     * @return
     */
    boolean isError();

    /**
     * 绑定失败策略类
     * 
     * @param strategy
     * @return this
     */
    InvocationFuture failStrategy(Class<? extends FailStrategy> strategyClass);

    /**
     * 获取失败策略
     * 
     * @param strategy
     */
    FailStrategy failStrategy();
}