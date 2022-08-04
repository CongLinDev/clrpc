package conglin.clrpc.service.publisher;

import conglin.clrpc.service.registry.ServiceRegistryFactory;

public interface Publisher {
    /**
     * 绑定registry
     * 
     * @param registryClass
     */
    void bindRegistryFactory(ServiceRegistryFactory registryFactory);
}
