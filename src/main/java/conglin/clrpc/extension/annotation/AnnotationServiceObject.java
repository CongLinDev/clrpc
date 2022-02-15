package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class AnnotationServiceObject<T> implements conglin.clrpc.service.ServiceObject<T> {
    private final T object;
    private final String name;
    private final conglin.clrpc.service.ServiceVersion version;
    private final Map<String, String> metaInfo;

    public AnnotationServiceObject(Class<T> serviceObjectClass) {
        ServiceObject serviceObject = serviceObjectClass.getAnnotation(ServiceObject.class);
        if (serviceObject == null) {
            throw new IllegalArgumentException(serviceObjectClass.getName() + "is not a service object");
        }

        object = ClassUtils.loadObjectByType(serviceObjectClass, serviceObjectClass);
        if (object == null) {
            throw new IllegalArgumentException(serviceObjectClass.getName() + "is not a service object");
        }

        String name = serviceObject.name();
        if ("".equals(name)) {
            name = serviceObjectClass.getName();
        }
        this.name = name;

        ServiceVersion serviceVersion = serviceObject.version();
        this.version = new conglin.clrpc.service.ServiceVersion(serviceVersion.major(), serviceVersion.minor(), serviceVersion.build());

        MetaInfo metaInfo = serviceObject.metaInfo();
        Map<String, String> metaInfoMap = new HashMap<>();
        for (Entry entry : metaInfo.entries()) {
            metaInfoMap.put(entry.key(), entry.value());
        }
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
    public Class<?> objectClass() {
        return object.getClass();
    }

    @Override
    public conglin.clrpc.service.ServiceVersion version() {
        return version;
    }
}
