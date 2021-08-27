package conglin.clrpc.thirdparty.fastjson.service;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.SimpleServiceObject;
import conglin.clrpc.thirdparty.fastjson.config.JsonPropertyConfigurer;

public class JsonServiceObjectBuilder {

    protected Object object;

    protected String name;

    protected PropertyConfigurer metaInfo = JsonPropertyConfigurer.empty();

    /**
     * 返回一个 builder
     *
     * @return
     */
    static JsonServiceObjectBuilder builder() {
        return new JsonServiceObjectBuilder();
    }

    /**
     * 构造 object
     *
     * @param object
     * @return
     */
    public JsonServiceObjectBuilder object(Object object) {
        this.object = object;
        return this;
    }

    /**
     * 构造 name
     *
     * @param name
     * @return
     */
    public JsonServiceObjectBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * 构造 meta
     *
     * @param key
     * @param value
     * @return
     */
    public JsonServiceObjectBuilder meta(String key, Object value) {
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
        if (name == null) {
           name = object.getClass().getName();
        }
        checkMeta();
        return new SimpleServiceObject(name, object, metaInfo);
    }

    /**
     * 检查元信息
     */
    protected void checkMeta() {
        metaInfo.put(ServiceObject.SERVICE_NAME, name);
        metaInfo.put(ServiceObject.OBJECT, object.getClass().getName());
    }

}
