package conglin.clrpc.service.proxy;

import java.util.function.Consumer;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.ServiceInstance;

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
    protected Class<? extends FailStrategy> failStrategyClass() {
        return serviceInterface.failStrategyClass();
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