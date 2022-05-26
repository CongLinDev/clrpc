package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;
import java.util.function.Consumer;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.ServiceMethodWrapper;
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
    protected String getMethodName(Method method) {
        return ServiceMethodWrapper.customMethodName(method);
    }

    @Override
    protected FailStrategy failStrategy() {
        return serviceInterface.failStrategy();
    }

    @Override
    protected Consumer<ServiceInstance> choosedInstancePostProcessor() {
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
