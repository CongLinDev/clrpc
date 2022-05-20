package conglin.clrpc.service.instance.codec;

import java.util.Map;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.service.AbstractServiceObject;
import conglin.clrpc.service.instance.AbstractServiceInstance;
import conglin.clrpc.service.instance.ServiceInstance;

public class DefaultServiceInstanceCodec implements ServiceInstanceCodec {
    @Override
    public ServiceInstance fromContent(String content) {
        Map<String, String> serviceMetaInfo = UrlScheme.resolveParameters(content, "[&]", "[=]");
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
        return UrlScheme.toParameters(serviceInstance.serviceObject().metaInfo(), "&", "=");
    }
}
