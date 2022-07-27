package conglin.clrpc.invocation.proxy;

import conglin.clrpc.executor.pipeline.ExecutorPipeline;
import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ComponentContextAware;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.Initializable;

public class ProxyTemplete implements InvocationProxy, Initializable, ComponentContextAware {

    private ComponentContext context;
    // 发送器
    private ExecutorPipeline executorPipeline;

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
        executorPipeline = getContext().getWith(ComponentContextEnum.EXECUTOR_PIPELINE);
    }

    /**
     * 异步调用函数 使用负载均衡策略
     *
     * @param invocationContext
     * @return
     */
    @Override
    public void call(InvocationContext invocationContext) {
        executorPipeline.outbound(invocationContext);
    }
}