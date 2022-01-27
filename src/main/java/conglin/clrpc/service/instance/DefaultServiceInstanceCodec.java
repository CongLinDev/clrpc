package conglin.clrpc.service.instance;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.service.AbstractServiceObject;
import conglin.clrpc.service.ServiceObject;

import java.util.Map;

public class DefaultServiceInstanceCodec implements ServiceInstanceCodec {
    @Override
    public ServiceInstance fromContent(String content) {
        Map<String, String> serviceMetaInfo = UrlScheme.resolveParameters(content, "[&]", "[=]");
        String address = serviceMetaInfo.get(ServiceInstance.INSTANCE_ADDRESS);
        return new AbstractServiceInstance(new AbstractServiceObject("", serviceMetaInfo) {
            @Override
            public Object object() {
                return null;
            }
        }, address){
            @Override
            public String toString() {
                return DefaultServiceInstanceCodec.this.toString(this);
            }
        };
    }

    protected String toString(AbstractServiceInstance serviceInstance) {
        return UrlScheme.toParameters(serviceInstance.serviceObject().metaInfo(), "&", "=");
    }

    @Override
    public String toString(ServiceObject serviceObject, String address) {
        serviceObject.metaInfo().putIfAbsent(ServiceInstance.INSTANCE_ADDRESS, address);
        return UrlScheme.toParameters(serviceObject.metaInfo(), "&", "=");
    }
}
