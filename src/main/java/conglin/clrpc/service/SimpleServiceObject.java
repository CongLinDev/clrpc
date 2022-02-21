package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;

public class SimpleServiceObject<T> extends AbstractServiceObject<T> {

    protected final T object;

    public SimpleServiceObject(Class<T> interfaceClass, T object, Map<String, String> metaInfo) {
        super(interfaceClass, metaInfo);
        this.object = object;
        metaInfo.put(ServiceObject.OBJECT, object.getClass().getName());
    }

    public SimpleServiceObject(String name, Class<T> interfaceClass, T object, Map<String, String> metaInfo) {
        super(name, interfaceClass, metaInfo);
        this.object = object;
        metaInfo.put(ServiceObject.OBJECT, object.getClass().getName());
    }

    @Override
    public T object() {
        return object;
    }

    @Override
    public Class<T> interfaceClass() {
        return interfaceClass;
    }

    /**
     * builder
     */
    public static class Builder<T> {
        protected Class<T> interfaceClass;
        protected T object;
        protected final Map<String, String> metaInfo;

        public Builder(Class<T> interfaceClass) {
            this.interfaceClass = interfaceClass;
            this.metaInfo = new HashMap<>();
        }

        /**
         * 构造 object
         *
         * @param object
         * @return
         */
        public Builder<T> object(T object) {
            this.object = object;
            return this;
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
         * 返回 ServiceObject
         *
         * @return
         */
        public SimpleServiceObject<T> build() {
            if (object == null)
                throw new IllegalArgumentException();
            return new SimpleServiceObject<>(interfaceClass, object, metaInfo);
        }
    }
}


