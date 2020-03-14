package conglin.clrpc.service.proxy;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;

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

    public AsyncObjectProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(serviceName, sender, identifierGenerator);
    }

    @Override
    protected Object handleFuture(RpcFuture future) throws Exception {
        threadLocal.set(future);
        return null;
    }
}