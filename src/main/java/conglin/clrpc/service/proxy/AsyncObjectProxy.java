package conglin.clrpc.service.proxy;

import java.util.function.Consumer;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.strategy.FailStrategy;

/**
 * 异步对象代理
 * 
 * 代理对象调用方法后 方法不阻塞直接返回默认值
 * 
 * 而 {@link InvocationFuture} 对象 保存在 当前线程的 {@code ThreadLocalMap} 中 可以调用
 * {@link InvocationFuture#lastFuture()} 获得
 * 
 * 需要注意的 {@code ThreadLocalMap} 只保存调用线程最新的 {@link InvocationFuture} 对象
 */
public class AsyncObjectProxy extends AbstractObjectProxy {

    private final ServiceInterface<?> serviceInterface;

    public AsyncObjectProxy(ServiceInterface<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    protected String getServiceName(Class<?> methodDeclaringClass) {
        return serviceInterface.name();
    }

    @Override
    protected FailStrategy failStrategy() {
        return serviceInterface.failStrategy();
    }

    @Override
    protected Object handleContext(InvocationContext invocationContext) throws Exception {
        InvocationContext.lastContext(invocationContext);
        return null;
    }

    @Override
    protected Consumer<ServiceInstance> instanceConsumer() {
        return null;
    }

    @Override
    protected InstanceCondition instanceCondition() {
        return serviceInterface.instanceCondition();
    }

    @Override
    protected long timeoutThreshold() {
        return serviceInterface.timeoutThreshold();
    }
}
