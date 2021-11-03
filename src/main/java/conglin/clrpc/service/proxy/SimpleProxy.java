package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.RequestWrapper;

public class SimpleProxy implements RpcProxy, Initializable, ContextAware {

    private RpcContext context;
    // 发送器
    private RequestSender sender;

    @Override
    public void setContext(RpcContext context) {
        this.context = context;
    }

    @Override
    public RpcContext getContext() {
        return context;
    }

    @Override
    public void init() {
        sender = getContext().getWith(RpcContextEnum.REQUEST_SENDER);
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
}