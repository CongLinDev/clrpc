package conglin.clrpc.service.future;

import java.util.concurrent.Future;
import java.util.function.Consumer;

import conglin.clrpc.common.Callback;

/**
 * future 对象 用于同步或异步地获取任务的执行结果
 * 
 * @see java.util.concurrent.Future
 */
public interface RpcFuture extends Future<Object> {

    /**
     * 添加回调
     * 
     * @param callback
     * @return this
     */
    RpcFuture callback(Callback callback);

    /**
     * 添加通用的回调
     * 
     * @param task
     * @return this
     */
    default RpcFuture callback(Runnable task) {
        return callback(Callback.convert(task));
    }

    /**
     * 添加通用的回调
     * 
     * @param any
     * @return this
     */
    default RpcFuture callback(Consumer<Object> any) {
        return callback(Callback.convert(any));
    }

    /**
     * 该 {@code RpcFuture} 的标识符
     * 
     * @return
     */
    long identifier();

    /**
     * 重试
     * 
     * @return 是否重试成功
     */
    boolean retry();

    /**
     * 获取重试次数
     * 
     * @return
     */
    int retryTimes();

    /**
     * 确认该 {@code RpcFuture} 完成
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
     * 该 {@link RpcFuture} 是否出错
     * 
     * 只有在{@link RpcFuture#isDone()} 返回值为 {@code true} 的情况下 该方法的返回值才可信
     * 
     * @return
     */
    boolean isError();

    /**
     * 是否超时
     * 
     * @param timeThreshold ms
     * @return
     */
    boolean timeout(long timeThreshold);
}