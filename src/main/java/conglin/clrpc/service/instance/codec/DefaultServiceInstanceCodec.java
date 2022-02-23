package conglin.clrpc.service.instance.codec;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.AbstractServiceObject;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.instance.AbstractServiceInstance;
import conglin.clrpc.service.instance.ServiceInstance;

import java.util.Map;

public class DefaultServiceInstanceCodec implements ServiceInstanceCodec {
    @Override
    public ServiceInstance fromContent(String content) {
        Map<String, String> serviceMetaInfo = UrlScheme.resolveParameters(content, "[&]", "[=]");
        String instanceId = serviceMetaInfo.get(ServiceInstance.INSTANCE_ID);
        String address = serviceMetaInfo.get(ServiceInstance.INSTANCE_ADDRESS);
        @SuppressWarnings("unchecked")
        Class<Object> interfaceClass = (Class<Object>) ClassUtils
                .loadClass(serviceMetaInfo.get(ServiceObject.INTERFACE));
        return new AbstractServiceInstance(instanceId, address,
                new AbstractServiceObject<>("", interfaceClass, serviceMetaInfo) {
                    @Override
                    public Object object() {
                        return null;
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
        return UrlScheme.toParameters(serviceInstance.serviceObject().metaInfo(), "&", "=");
    }
}
