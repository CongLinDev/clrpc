package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.RequestWrapper;

public class SimpleProxy implements InvocationProxy, Initializable, ComponentContextAware {

    private ComponentContext context;
    // 发送器
    private RequestSender sender;

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    public void init() {
        sender = getContext().getWith(ComponentContextEnum.REQUEST_SENDER);
    }

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param requestWrapper
     * @return
     */
    @Override
    public InvocationFuture call(RequestWrapper requestWrapper) {
        return sender.sendRequest(requestWrapper);
    }
}