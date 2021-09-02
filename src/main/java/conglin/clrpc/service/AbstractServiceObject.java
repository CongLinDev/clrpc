package conglin.clrpc.service;

import conglin.clrpc.common.config.PropertyConfigurer;

abstract public class AbstractServiceObject implements ServiceObject {

    protected final PropertyConfigurer metaInfo;

    public AbstractServiceObject(PropertyConfigurer metaInfo) {
        this.metaInfo = metaInfo;
    }

    public AbstractServiceObject(String name, PropertyConfigurer metaInfo) {
        this.metaInfo = metaInfo;
        metaInfo.put(ServiceObject.SERVICE_NAME, name);
    }

    @Override
    public String name() {
        Object serviceName = metaInfo.putIfAbsent(ServiceObject.SERVICE_NAME, object().getClass().getName());
        return (String)serviceName;
    }

    @Override
    public PropertyConfigurer metaInfo() {
        return metaInfo;
    }

    @Override
    public ServiceVersion version() {
        Object version = metaInfo.putIfAbsent(ServiceObject.VERSION, ServiceVersion.defaultVersion());
        return (ServiceVersion)version;
    }

}
