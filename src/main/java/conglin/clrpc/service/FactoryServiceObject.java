package conglin.clrpc.service;

import java.util.Map;
import java.util.function.Supplier;

abstract public class FactoryServiceObject extends AbstractServiceObject {

    protected final Supplier<?> factory;

    protected final Class<?> objectClass;

    public FactoryServiceObject(Supplier<?> factory, Class<?> objectClass, Map<String, String> metaInfo) {
        super(factory.getClass().getName(), metaInfo);
        this.factory = factory;
        this.objectClass = objectClass;
    }

    public FactoryServiceObject(String name, Supplier<?> factory, Class<?> objectClass, Map<String, String> metaInfo) {
        super(name, metaInfo);
        this.factory = factory;
        this.objectClass = objectClass;
    }

    @Override
    public Object object() {
        return factory.get();
    }

    @Override
    public Class<?> objectClass() {
        return objectClass;
    }
}
