package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class FactoryServiceObject<T> extends AbstractServiceObject<T> {

    protected final Supplier<T> factory;

    protected FactoryServiceObject(Class<T> interfaceClass, Supplier<T> factory, Map<String, String> metaInfo) {
        this(interfaceClass.getName(), interfaceClass, factory, metaInfo);
    }

    protected FactoryServiceObject(String name, Class<T> interfaceClass, Supplier<T> factory, Map<String, String> metaInfo) {
        super(name, interfaceClass, metaInfo);
        this.factory = factory;
    }

    @Override
    public T object() {
        return factory.get();
    }

    /**
     * builder
     */
    public static class Builder<T> {
        protected Supplier<T> factory;
        protected final Map<String, String> metaInfo;
        protected Class<T> interfaceClass;

        public Builder(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
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
            return new FactoryServiceObject<>(interfaceClass, factory, metaInfo);
        }
    }
}
