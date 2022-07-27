package conglin.clrpc.invocation.proxy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.future.InvocationFuture;
import conglin.clrpc.service.ServiceInterface;

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
public class AsyncObjectProxy extends ServiceInterfaceObjectProxy {
    public AsyncObjectProxy(ServiceInterface<?> serviceInterface) {
        super(serviceInterface);
    }

    @Override
    protected Object handleContext(InvocationContext invocationContext) throws Exception {
        InvocationContext.lastContext(invocationContext);
        return null;
    }
}
