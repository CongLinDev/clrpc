package conglin.clrpc.thirdparty.fastjson.util;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.AbstractServiceObject;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.thirdparty.fastjson.config.JsonPropertyConfigurer;

public class JsonServiceObjectUtils {

    public static ServiceObject fromContent(String content) {
        PropertyConfigurer configurer = JsonPropertyConfigurer.fromContent(content);
        String name = configurer.get(ServiceObject.SERVICE_NAME, String.class);
        if (name == null) {
            throw new IllegalArgumentException("service name can not be null");
        }
        return new AbstractServiceObject(configurer) {
            @Override
            public Object object() {
                return null;
            }
            @Override
            public Class<?> objectClass() {
                return null;
            }
        };
    }

    public static String toContent(ServiceObject serviceObject) {
        return serviceObject.metaInfo().toString();
    }
}
