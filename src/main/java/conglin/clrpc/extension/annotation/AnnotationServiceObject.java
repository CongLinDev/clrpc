package conglin.clrpc.extension.annotation;

import conglin.clrpc.common.config.MapPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.util.ClassUtils;

public class AnnotationServiceObject implements conglin.clrpc.service.ServiceObject {
    private final Object object;
    private final String name;
    private final conglin.clrpc.service.ServiceVersion version;
    private final PropertyConfigurer configurer;

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
        Class<? extends PropertyConfigurer> metaClass = metaInfo.metaClass();
        PropertyConfigurer c = null;
        if (metaClass.isInterface() || (c = ClassUtils.loadObjectByType(metaClass, PropertyConfigurer.class)) == null) {
            c = new MapPropertyConfigurer();
        }
        for (Entry entry : metaInfo.entries()) {
            c.put(entry.key(), entry.value());
        }
        configurer = c;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public PropertyConfigurer metaInfo() {
        return configurer;
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
