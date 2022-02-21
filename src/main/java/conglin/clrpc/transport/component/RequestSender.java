package conglin.clrpc.transport.component;

import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.future.InvocationFuture;

public interface RequestSender {

    /**
     * 发送请求信息
     *
     * @param invocationContext
     * @return
     */
    InvocationFuture sendRequest(InvocationContext invocationContext);
}