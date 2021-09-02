package conglin.clrpc.thirdparty.fastjson.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import conglin.clrpc.router.instance.AbstractServiceInstance;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.thirdparty.fastjson.util.JsonServiceObjectUtils;

import java.util.HashMap;
import java.util.Map;

public class JsonServiceInstance extends AbstractServiceInstance {

    public JsonServiceInstance(ServiceObject serviceObject, String address) {
        super(serviceObject, address);
    }

    public static JsonServiceInstance fromContent(String content) {
        JSONObject jsonObject = JSON.parseObject(content);
        String address = jsonObject.getString(INSTANCE_ADDRESS);
        String serviceObject = jsonObject.getString(INSTANCE_OBJECT);
        return new JsonServiceInstance(JsonServiceObjectUtils.fromContent(serviceObject), address);
    }

    public static String toContent(JsonServiceInstance serviceInstance) {
        return serviceInstance.toString();
    }

    @Override
    public String toString() {
        Map<String, Object> map = new HashMap<>();
        map.put(INSTANCE_ADDRESS, address);
        map.put(INSTANCE_OBJECT, JsonServiceObjectUtils.toContent(serviceObject));
        return new JSONObject(map).toJSONString();
    }
}
