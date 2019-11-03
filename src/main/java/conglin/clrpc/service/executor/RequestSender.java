package conglin.clrpc.service.executor;

import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;

public interface RequestSender {
    /**
     * 发送请求信息
     * @param request
     * @return
     */
    RpcFuture sendRequest(BasicRequest request);
    
    /**
     * 向指定服务提供者发送请求
     * @param remoteAddress 指定服务提供者地址
     * @param request
     * @return
     */
    RpcFuture sendRequest(String remoteAddress, BasicRequest request);

    /**
     * 重发请求信息
     * @param request
     */
    void resendRequest(BasicRequest request);

    /**
     * 向指定服务提供者重发请求信息
     * @param remoteAddress 指定服务提供者地址
     * @param request
     */
    void resendRequest(String remoteAddress, BasicRequest request);
}