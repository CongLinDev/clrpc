package conglin.clrpc.service.future;

import java.util.concurrent.Future;

import conglin.clrpc.common.Callback;

public interface RpcFuture extends Future<Object> {

    /**
     * 添加回调函数
     * 
     * @param callback
     * @return
     */
    boolean addCallback(Callback callback);

    /**
     * 该 {@code RpcFuture} 的标识符
     * 
     * @return
     */
    long identifier();

    /**
     * 重试
     */
    void retry();

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
     * 该 {@link RpcFuture} 是否出错 只有在{@link RpcFuture#isDone()} 返回值为 true 的情况下
     * 该方法的返回值才可信
     * 
     * @return
     */
    boolean isError();

    /**
     * 是否超时
     * 
     * @return
     */
    boolean timeout();
}