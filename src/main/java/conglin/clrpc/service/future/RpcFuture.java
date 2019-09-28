package conglin.clrpc.service.future;

import java.util.concurrent.Future;

import conglin.clrpc.common.Callback;

public interface RpcFuture extends Future<Object> {

    
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
     * 以下为状态相关方法
     */

    /**
     * 是否等待中
     * @return
     */
    boolean isPending();

    /**
     * 是否出错
     * @return
     */
    boolean isError();

    /**
     * 是否超时
     * @return
     */
    boolean timeout();
}