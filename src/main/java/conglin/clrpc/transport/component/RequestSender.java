package conglin.clrpc.transport.component;

import conglin.clrpc.service.context.InvocationContext;

public interface RequestSender {

    /**
     * 发送请求信息
     *
     * @param invocationContext
     * @return
     */
    void send(InvocationContext invocationContext);
}