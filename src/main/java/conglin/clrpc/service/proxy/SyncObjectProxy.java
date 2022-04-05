package conglin.clrpc.service.proxy;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.context.InvocationContext;

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