package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.Fallback;
import conglin.clrpc.common.util.ClassUtils;

public class AnnotationServiceInterface<T> implements conglin.clrpc.service.ServiceInterface<T> {

    private final Class<T> serviceInterfaceClass;
    private final String name;
    private final Fallback fallback;
    private final conglin.clrpc.service.ServiceVersion version;

    public AnnotationServiceInterface(Class<T> serviceInterfaceClass) {
        this.serviceInterfaceClass = serviceInterfaceClass;

        ServiceInterface serviceInterface = serviceInterfaceClass.getAnnotation(ServiceInterface.class);
        if (serviceInterface == null) {
            throw new IllegalArgumentException(serviceInterfaceClass.getName() + "is not a service interface");
        }

        String name = serviceInterface.name();
        if ("".equals(name)) {
            name = serviceInterfaceClass.getName();
        }
        this.name = name;

        Class<? extends Fallback> fallbackClass = serviceInterface.fallback();
        fallback = fallbackClass.isInterface() ? null : ClassUtils.loadObjectByType(fallbackClass, Fallback.class);

        ServiceVersion serviceVersion = serviceInterface.version();
        this.version = new conglin.clrpc.service.ServiceVersion(serviceVersion.major(), serviceVersion.minor(), serviceVersion.build());
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
    public Fallback fallback() {
        return fallback;
    }

    @Override
    public conglin.clrpc.service.ServiceVersion version() {
        return version;
    }
}
