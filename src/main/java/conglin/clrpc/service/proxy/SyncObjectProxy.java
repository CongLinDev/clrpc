package conglin.clrpc.service.proxy;

import conglin.clrpc.common.Fallback;
import conglin.clrpc.router.instance.ServiceInstance;
import conglin.clrpc.service.ServiceInterface;

import java.util.function.Consumer;

/**
 * 同步对象代理
 * 
 * 代理对象调用方法后 方法阻塞直至返回结果
 */
public class SyncObjectProxy extends AbstractObjectProxy {

    private final ServiceInterface<?> serviceInterface;

    public SyncObjectProxy(ServiceInterface<?> serviceInterface) {
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

    @Override
    protected Consumer<ServiceInstance> beforeSendRequest() {
        return null;
    }
}