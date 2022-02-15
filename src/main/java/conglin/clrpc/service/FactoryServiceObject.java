package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FactoryServiceObject<T> extends AbstractServiceObject<T> {

    protected final Supplier<T> factory;

    protected final Class<?> objectClass;

    protected FactoryServiceObject(Supplier<T> factory, Map<String, String> metaInfo) {
        this(factory.getClass().getName(), factory, metaInfo);
    }

    protected FactoryServiceObject(String name, Supplier<T> factory, Map<String, String> metaInfo) {
        super(name, metaInfo);
        this.factory = factory;
        this.objectClass = factory.get().getClass();
    }

    @Override
    public T object() {
        return factory.get();
    }

    @Override
    public Class<?> objectClass() {
        return objectClass;
    }

    /**
     * builder
     */
    public static class Builder<T> {
        protected Supplier<T> factory;
        protected final Map<String, String> metaInfo;
        protected Class<T> objectClass;

        public Builder() {
            this.metaInfo = new HashMap<>();
        }

        /**
         * 构造 name
         *
         * @param name
         * @return
         */
        public Builder<T> name(String name) {
            return meta(ServiceObject.SERVICE_NAME, name);
        }

        /**
         * 构造 meta
         *
         * @param key
         * @param value
         * @return
         */
        public Builder<T> meta(String key, String value) {
            metaInfo.putIfAbsent(key, value);
            return this;
        }

        /**
         * factory
         * 
         * @param factory
         * @return
         */
        public Builder<T> factory(Supplier<T> factory) {
            this.factory = factory;
            return this;
        }

        /**
         * 返回 ServiceObject
         *
         * @return
         */
        public FactoryServiceObject<T> build() {
            if (factory == null)
                throw new IllegalArgumentException();
            return new FactoryServiceObject<>(SERVICE_NAME, factory, metaInfo);
        }
    }
}
