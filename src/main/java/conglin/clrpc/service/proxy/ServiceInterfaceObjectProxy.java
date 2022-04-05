package conglin.clrpc.service.proxy;

import java.util.function.Consumer;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.strategy.FailStrategy;

/**
 * binding {@link ServiceInterface}
 */
abstract public class ServiceInterfaceObjectProxy extends AbstractObjectProxy {
    protected final ServiceInterface<?> serviceInterface;

    public ServiceInterfaceObjectProxy(ServiceInterface<?> serviceInterface) {
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
