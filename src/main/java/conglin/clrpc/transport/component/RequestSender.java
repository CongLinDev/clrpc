package conglin.clrpc.transport.component;

import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicRequest;

public interface RequestSender {
    /**
     * 发送请求信息
     * 
     * @param request
     * @return
     */
    default RpcFuture sendRequest(BasicRequest request) {
        return sendRequest(request, null);
    }

    /**
     * 向指定服务提供者发送请求
     * 
     * 当地址为 {@code null} 时调用 {@link RequestSender#sendRequest(BasicRequest)}
     * 
     * @param request
     * @param remoteAddress 指定服务提供者地址
     * @return
     */
    RpcFuture sendRequest(BasicRequest request, String remoteAddress);

    /**
     * 重发请求信息
     * 
     * @param request
     */
    default void resendRequest(BasicRequest request) {
        resendRequest(request, null);
    }

    /**
     * 向指定服务提供者重发请求信息
     * 
     * 当地址为 {@code null} 时调用 {@link RequestSender#resendRequest(BasicRequest)}
     * 
     * @param request
     * @param remoteAddress 指定服务提供者地址
     */
    void resendRequest(BasicRequest request, String remoteAddress);
}