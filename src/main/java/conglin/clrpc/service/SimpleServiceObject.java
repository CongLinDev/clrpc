package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;

public class SimpleServiceObject<T> extends AbstractServiceObject<T> {

    protected final T object;

    public SimpleServiceObject(T object, Map<String, String> metaInfo) {
        super(object.getClass().getName(), metaInfo);
        this.object = object;
        metaInfo.putIfAbsent(ServiceObject.OBJECT, objectClass().getName());
    }

    public SimpleServiceObject(String name, T object, Map<String, String> metaInfo) {
        super(name, metaInfo);
        this.object = object;
        metaInfo.putIfAbsent(ServiceObject.OBJECT, objectClass().getName());
    }

    @Override
    public T object() {
        return object;
    }

    /**
     * builder
     */
    public static class Builder<T> {
        protected T object;
        protected final Map<String, String> metaInfo;

        public Builder() {
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
            return new SimpleServiceObject<>(object, metaInfo);
        }
    }
}


