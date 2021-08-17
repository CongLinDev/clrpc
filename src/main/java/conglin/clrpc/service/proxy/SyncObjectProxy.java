package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Fallback;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.transport.component.RequestSender;

/**
 * 同步对象代理
 * 
 * 代理对象调用方法后 方法阻塞直至返回结果
 */
public class SyncObjectProxy extends BasicProxy {

    private final ServiceInterface<?> serviceInterface;

    public SyncObjectProxy(ServiceInterface<?> serviceInterface, RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender, identifierGenerator);
        this.serviceInterface = serviceInterface;
    }

    @Override
    protected Fallback fallback() {
        return serviceInterface.fallback();
    }

    @Override
    protected String getServiceName(Class<?> methodDeclaringClass) {
        return serviceInterface.name();
    }
}