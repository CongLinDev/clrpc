package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.util.ClassUtils;

import java.util.HashMap;
import java.util.Map;

public class AnnotationServiceObject implements conglin.clrpc.service.ServiceObject {
    private final Object object;
    private final String name;
    private final conglin.clrpc.service.ServiceVersion version;
    private final Map<String, String> metaInfo;

    public AnnotationServiceObject(Class<?> serviceObjectClass) {
        ServiceObject serviceObject = serviceObjectClass.getAnnotation(ServiceObject.class);
        if (serviceObject == null) {
            throw new IllegalArgumentException(serviceObjectClass.getName() + "is not a service object");
        }

        object = ClassUtils.loadObject(serviceObjectClass);
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
    public Object object() {
        return object;
    }

    @Override
    public Class<?> objectClass() {
        return conglin.clrpc.service.ServiceObject.super.objectClass();
    }

    @Override
    public conglin.clrpc.service.ServiceVersion version() {
        return version;
    }
}
