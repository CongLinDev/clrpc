package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class AnnotationServiceObject<T> implements conglin.clrpc.service.ServiceObject<T> {
    private final Class<T> interfaceClass;
    private final T object;
    private final String name;
    private final conglin.clrpc.service.ServiceVersion version;
    private final Map<String, String> metaInfo;

    public AnnotationServiceObject(Class<?> serviceObjectClass) {
        ServiceObject serviceObject = serviceObjectClass.getAnnotation(ServiceObject.class);
        if (serviceObject == null) {
            throw new IllegalArgumentException(serviceObjectClass.getName() + "is not a service object");
        }
        @SuppressWarnings("unchecked")
        Class<T> interfaceClass = (Class<T>)serviceObject.interfaceClass();
        if (!interfaceClass.isAssignableFrom(serviceObjectClass)) {
            throw new IllegalArgumentException(interfaceClass.getName() + "is not assignable from " + serviceObjectClass.getName());
        }
        this.interfaceClass = interfaceClass;
        // assert 
        object = ClassUtils.loadObjectByType(serviceObjectClass, interfaceClass);
        if (object == null) {
            throw new IllegalArgumentException(serviceObjectClass.getName() + "is not a service object");
        }

        String name = serviceObject.name();
        if ("".equals(name)) {
            name = interfaceClass.getName();
        }
        this.name = name;

        ServiceVersion serviceVersion = serviceObject.version();
        this.version = new conglin.clrpc.service.ServiceVersion(serviceVersion.major(), serviceVersion.minor(), serviceVersion.build());

        MetaInfo metaInfo = serviceObject.metaInfo();
        Map<String, String> metaInfoMap = new HashMap<>();
        for (Entry entry : metaInfo.entries()) {
            metaInfoMap.put(entry.key(), entry.value());
        }
        metaInfoMap.put(conglin.clrpc.service.ServiceObject.OBJECT, serviceObjectClass.getName());
        metaInfoMap.put(conglin.clrpc.service.ServiceObject.INTERFACE, interfaceClass.getName());
        metaInfoMap.put(conglin.clrpc.service.ServiceObject.SERVICE_NAME, name);
        metaInfoMap.put(conglin.clrpc.service.ServiceObject.VERSION, this.version.toString());
        this.metaInfo = metaInfoMap;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public Map<String, String> metaInfo() {
        return this.metaInfo;
    }

    @Override
    public T object() {
        return object;
    }

    @Override
    public Class<T> interfaceClass() {
        return interfaceClass;
    }

    @Override
    public conglin.clrpc.service.ServiceVersion version() {
        return version;
    }
}
