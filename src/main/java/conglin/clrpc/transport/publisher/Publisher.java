package conglin.clrpc.transport.publisher;

import conglin.clrpc.service.registry.ServiceRegistry;

public interface Publisher {
    /**
     * 绑定registry
     * 
     * @param registryClass
     */
    void bindRegistry(Class<? extends ServiceRegistry> registryClass);
}
