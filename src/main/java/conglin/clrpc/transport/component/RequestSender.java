package conglin.clrpc.transport.component;

import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.RequestWrapper;

public interface RequestSender {

    /**
     * 发送请求信息
     *
     * @param requestWrapper
     * @return
     */
    RpcFuture sendRequest(RequestWrapper requestWrapper);
}