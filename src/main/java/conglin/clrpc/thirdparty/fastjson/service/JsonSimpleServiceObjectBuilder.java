package conglin.clrpc.thirdparty.fastjson.service;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.ServiceVersion;
import conglin.clrpc.service.SimpleServiceObject;
import conglin.clrpc.thirdparty.fastjson.config.JsonPropertyConfigurer;

public class JsonSimpleServiceObjectBuilder {

    protected Object object;

    protected PropertyConfigurer metaInfo = JsonPropertyConfigurer.empty();

    /**
     * 返回一个 builder
     *
     * @return
     */
    static JsonSimpleServiceObjectBuilder builder() {
        return new JsonSimpleServiceObjectBuilder();
    }

    /**
     * 构造 object
     *
     * @param object
     * @return
     */
    public JsonSimpleServiceObjectBuilder object(Object object) {
        this.object = object;
        return this;
    }

    /**
     * 构造 name
     *
     * @param name
     * @return
     */
    public JsonSimpleServiceObjectBuilder name(String name) {
        return meta(ServiceObject.SERVICE_NAME, name);
    }

    /**
     * 构造 meta
     *
     * @param key
     * @param value
     * @return
     */
    public JsonSimpleServiceObjectBuilder meta(String key, Object value) {
        metaInfo.put(key, value);
        return this;
    }

    /**
     * 返回 ServiceObject
     *
     * @return
     */
    public ServiceObject build() {
        if(object == null)
            throw new IllegalArgumentException();
        checkMeta();
        return new SimpleServiceObject(object, metaInfo);
    }

    /**
     * 检查元信息
     */
    protected void checkMeta() {
        metaInfo.putIfAbsent(ServiceObject.SERVICE_NAME, object.getClass().getName());
        metaInfo.putIfAbsent(ServiceObject.OBJECT, object.getClass().getName());
        metaInfo.putIfAbsent(ServiceObject.VERSION, ServiceVersion.defaultVersion());
    }

}
