package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;

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
     * 标记 {@link InvocationFuture} 完成
     * 
     * @param needSignError 是否需要标记为错误
     * @param result        结果
     * @return 是否成功
     */
    boolean done(boolean needSignError, Object result);

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
}