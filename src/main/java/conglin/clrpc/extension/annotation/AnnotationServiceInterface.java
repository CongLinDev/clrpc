package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.invocation.strategy.FailStrategy;
import conglin.clrpc.service.instance.condition.InstanceCondition;

public class AnnotationServiceInterface<T> implements conglin.clrpc.service.ServiceInterface<T> {

    private final Class<T> serviceInterfaceClass;
    private final String name;
    private final FailStrategy failStrategy;
    private final conglin.clrpc.service.ServiceVersion version;
    private final InstanceCondition instanceCondition;
    private final long timeoutThreshold;
    
    public AnnotationServiceInterface(Class<T> serviceInterfaceClass) {
        this.serviceInterfaceClass = serviceInterfaceClass;

        ServiceInterface serviceInterface = serviceInterfaceClass.getAnnotation(ServiceInterface.class);
        if (serviceInterface == null) {
            throw new IllegalArgumentException(serviceInterfaceClass.getName() + "is not a service interface");
        }

        String name = serviceInterface.name();
        if (name.isEmpty()) {
            name = serviceInterfaceClass.getName();
        }
        this.name = name;

        this.timeoutThreshold = serviceInterface.timeoutThreshold();

        this.failStrategy = ClassUtils.loadObjectByType(serviceInterface.failStrategy(), FailStrategy.class);
        ServiceVersion serviceVersion = serviceInterface.version();
        this.version = new conglin.clrpc.service.ServiceVersion(serviceVersion.major(), serviceVersion.minor(), serviceVersion.build());

        this.instanceCondition = ClassUtils.loadObjectByType(serviceInterface.conditionClass(), InstanceCondition.class);
        instanceCondition.bindServiceInterface(this);
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Class<T> interfaceClass() {
        return serviceInterfaceClass;
    }

    @Override
    public FailStrategy failStrategy() {
        return failStrategy;
    }

    @Override
    public conglin.clrpc.service.ServiceVersion version() {
        return version;
    }

    @Override
    public InstanceCondition instanceCondition() {
        return instanceCondition;
    }

    @Override
    public long timeoutThreshold() {
        return timeoutThreshold;
    }
}
