package conglin.clrpc.service.proxy;

import java.util.function.Consumer;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;

/**
 * 异步对象代理
 * 
 * 代理对象调用方法后 方法不阻塞直接返回默认值
 * 
 * 而 {@link RpcFuture} 对象 保存在 当前线程的 {@code ThreadLocalMap} 中 可以调用
 * {@link AsyncObjectProxy#lastFuture()} 获得
 * 
 * 需要注意的 {@code ThreadLocalMap} 只保存调用线程最新的 {@link RpcFuture} 对象
 */
public class AsyncObjectProxy extends AbstractObjectProxy {

    private static final ThreadLocal<RpcFuture> threadLocal = new ThreadLocal<>();

    /**
     * 返回当前线程最新一次操作产生的 future 对象
     * 
     * @return
     */
    public static RpcFuture lastFuture() {
        return threadLocal.get();
    }

    /**
     * 移除当前线程最新一次操作产生的 future 对象
     */
    public static void removeFuture() {
        threadLocal.remove();
    }

    private final ServiceInterface<?> serviceInterface;

    public AsyncObjectProxy(ServiceInterface<?> serviceInterface) {
        this.serviceInterface = serviceInterface;
    }

    @Override
    protected String getServiceName(Class<?> methodDeclaringClass) {
        return serviceInterface.name();
    }

    @Override
    protected Class<? extends FailStrategy> failStrategyClass() {
        return serviceInterface.failStrategyClass();
    }

    @Override
    protected Object handleFuture(RpcFuture future) throws Exception {
        threadLocal.set(future);
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
}
