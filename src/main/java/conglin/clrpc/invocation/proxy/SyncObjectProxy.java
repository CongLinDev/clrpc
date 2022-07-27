package conglin.clrpc.invocation.proxy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.service.ServiceInterface;

/**
 * 同步对象代理
 * 
 * 代理对象调用方法后 方法阻塞直至返回结果
 */
public class SyncObjectProxy extends ServiceInterfaceObjectProxy {

    public SyncObjectProxy(ServiceInterface<?> serviceInterface) {
        super(serviceInterface);
    }

    @Override
    protected Object handleContext(InvocationContext invocationContext) throws Exception {
        return invocationContext.getFuture().get();
    }

}