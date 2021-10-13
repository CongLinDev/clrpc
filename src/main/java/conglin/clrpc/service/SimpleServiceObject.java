package conglin.clrpc.service;

import conglin.clrpc.common.config.PropertyConfigurer;

public class SimpleServiceObject extends AbstractServiceObject {

    protected final Object object;

    public SimpleServiceObject(Object object, PropertyConfigurer metaInfo) {
        super(metaInfo);
        this.object = object;
    }

    public SimpleServiceObject(String name, Object object, PropertyConfigurer metaInfo) {
        super(name, metaInfo);
        this.object = object;
    }

    @Override
    public Object object() {
        return object;
    }
}
