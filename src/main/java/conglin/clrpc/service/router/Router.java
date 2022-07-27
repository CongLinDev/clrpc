package conglin.clrpc.service.router;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.registry.ServiceRegistry;

public interface Router {
    /**
     * choose
     *
     * @param condition
     * @return
     * @throws NoAvailableServiceInstancesException
     */
    RouterResult choose(RouterCondition condition) throws NoAvailableServiceInstancesException;

    /**
     * 订阅服务
     * 
     * @param serviceInterface
     * @param loadBalancerClass
     */
    void subscribe(ServiceInterface<?> serviceInterface, Class<?> loadBalancerClass);

    /**
     * 绑定registry
     * 
     * @param registryClass
     */
    void bindRegistry(Class<? extends ServiceRegistry> registryClass);
}
