package conglin.clrpc.service;

import conglin.clrpc.common.config.PropertyConfigurer;

public class SimpleServiceObject implements ServiceObject {

    protected final Object object;

    protected final String name;

    protected final PropertyConfigurer metaInfo;

    public SimpleServiceObject(String name, Object object, PropertyConfigurer metaInfo) {
        this.object = object;
        this.name = name;
        this.metaInfo = metaInfo;
    }

    @Override
    public String name() {
        return name;
    }

    @Override
    public PropertyConfigurer metaInfo() {
        return metaInfo;
    }

    @Override
    public Object object() {
        return object;
    }
}
