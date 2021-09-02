package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Fallback;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.RequestWrapper;

public class CommonProxy implements RpcProxy {

    // 发送器
    private final RequestSender sender;

    public CommonProxy(RequestSender sender) {
        this.sender = sender;
    }

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param requestWrapper
     * @return
     */
    @Override
    public RpcFuture call(RequestWrapper requestWrapper) {
        return sender.sendRequest(requestWrapper);
    }

    /**
     * 请求发送器
     * 
     * @return
     */
    protected RequestSender requestSender() {
        return this.sender;
    }

    /**
     * fallback
     *
     * @return
     */
    protected Fallback fallback() {
        return null;
    }
}