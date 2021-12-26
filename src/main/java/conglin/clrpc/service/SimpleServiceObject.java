package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;

public class SimpleServiceObject extends AbstractServiceObject {

    protected final Object object;

    public SimpleServiceObject(Object object, Map<String, String> metaInfo) {
        super(object.getClass().getName(), metaInfo);
        this.object = object;
        metaInfo.putIfAbsent(ServiceObject.OBJECT, objectClass().getName());
    }

    public SimpleServiceObject(String name, Object object, Map<String, String> metaInfo) {
        super(name, metaInfo);
        this.object = object;
        metaInfo.putIfAbsent(ServiceObject.OBJECT, objectClass().getName());
    }

    @Override
    public Object object() {
        return object;
    }

    /**
     * builder
     */
    public static class Builder {
        protected Object object;
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
        public Builder object(Object object) {
            this.object = object;
            return this;
        }

        /**
         * 构造 name
         *
         * @param name
         * @return
         */
        public Builder name(String name) {
            return meta(ServiceObject.SERVICE_NAME, name);
        }

        /**
         * 构造 meta
         *
         * @param key
         * @param value
         * @return
         */
        public Builder meta(String key, String value) {
            metaInfo.putIfAbsent(key, value);
            return this;
        }

        /**
         * 返回 ServiceObject
         *
         * @return
         */
        public SimpleServiceObject build() {
            if (object == null)
                throw new IllegalArgumentException();
            return new SimpleServiceObject(object, metaInfo);
        }
    }
}


