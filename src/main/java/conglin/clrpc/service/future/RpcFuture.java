package conglin.clrpc.service.future;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RequestException;

public interface RpcFuture {
    /**
     * 获取结果
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws RequestException
     */
    Object get() throws InterruptedException, ExecutionException, RequestException;

    /**
     * 在指定时间内获取结果
     * @param timeout
     * @param unit
     * @return
     * @throws InterruptedException
     * @throws ExecutionException
     * @throws TimeoutException
     * @throws RequestException
     */
    Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException, RequestException;
    
    /**
     * 添加回调函数
     * @param callback
     */
    void addCallback(Callback callback);

    /**
     * 该 {@code RpcFuture} 的标识符
     * @return
     */
    long identifier();

    /**
     * 重试
     */
    void retry();

    /**
     * 确认该 {@code RpcFuture} 完成
     * @param result
     */
    void done(Object result);

    /**
     * 取消
     * @param mayInterruptIfRunning
     * @return
     */
    boolean cancel(boolean mayInterruptIfRunning);


    /**
     * 以下为状态相关方法
     */

    /**
     * 是否完成
     * @return
     */
    boolean isDone();

    /**
     * 是否取消
     * @return
     */
    boolean isCancelled();

    /**
     * 是否等待中
     * @return
     */
    boolean isPending();

    /**
     * 该 {@link RpcFuture} 是否出错
     * 只有在{@link RpcFuture#isDone()} 返回值为 true 的情况下
     * 该方法的返回值才可信
     * @return
     */
    boolean isError();

    /**
     * 是否超时
     * @return
     */
    boolean timeout();
}