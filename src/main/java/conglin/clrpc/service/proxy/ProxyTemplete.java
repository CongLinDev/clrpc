package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.transport.component.InvocationExecutor;

public class ProxyTemplete implements InvocationProxy, Initializable, ComponentContextAware {

    private ComponentContext context;
    // 发送器
    private InvocationExecutor executor;

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
        executor = getContext().getWith(ComponentContextEnum.INVOCATION_EXECUTOR);
    }

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param invocationContext
     * @return
     */
    @Override
    public void call(InvocationContext invocationContext) {
        executor.execute(invocationContext);
    }
}