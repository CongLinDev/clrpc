package conglin.clrpc.service.instance.codec;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import conglin.clrpc.service.AbstractServiceObject;
import conglin.clrpc.service.instance.AbstractServiceInstance;
import conglin.clrpc.service.instance.ServiceInstance;

public class DefaultServiceInstanceCodec implements ServiceInstanceCodec {
    @Override
    public ServiceInstance fromContent(String content) {
        Map<String, String> serviceMetaInfo = resolveParameters(content, "[&]", "[=]");
        return new AbstractServiceInstance(serviceMetaInfo.get(ServiceInstance.INSTANCE_ID),
                serviceMetaInfo.get(ServiceInstance.INSTANCE_ADDRESS),
                new AbstractServiceObject<>("", Object.class, serviceMetaInfo) {
                    @Override
                    public Object object() {
                        throw new UnsupportedOperationException();
                    }

                    @Override
                    public Class<Object> interfaceClass() {
                        throw new UnsupportedOperationException();
                    }
                }) {
            @Override
            public String toString() {
                return DefaultServiceInstanceCodec.this.toContent(this);
            }
        };
    }

    @Override
    public String toContent(ServiceInstance serviceInstance) {
        return toParameters(serviceInstance.serviceObject().metaInfo(), "&", "=");
    }

        /**
     * 解析参数
     *
     * @param parameterString
     * @param paramsSplitRegex
     * @param keyValueSplitRegex
     * @return
     */
    public static Map<String, String> resolveParameters(String parameterString, String paramsSplitRegex,
            String keyValueSplitRegex) {
        if (parameterString == null || parameterString.isEmpty()) {
            return Collections.emptyMap();
        }

        String[] params = parameterString.split(paramsSplitRegex);
        Map<String, String> map = new HashMap<>();
        for (String param : params) {
            String[] pair = param.split(keyValueSplitRegex);
            if (pair.length < 2)
                continue;
            map.put(pair[0], pair[1]);
        }
        return map;
    }

    /**
     * 转换参数
     *
     * @param parameters
     * @param paramsJoin
     * @param keyValueJoin
     * @return
     */
    public static String toParameters(Map<String, String> parameters, String paramsJoin, String keyValueJoin) {
        if (parameters.isEmpty())
            return "";

        StringBuilder builder = new StringBuilder();
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.append(entry.getKey())
                    .append(keyValueJoin)
                    .append(entry.getValue() == null ? "" : entry.getValue())
                    .append(paramsJoin);
        }
        return builder.substring(0, builder.length() - paramsJoin.length());
    }
}
