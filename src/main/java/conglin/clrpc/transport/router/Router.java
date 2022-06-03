package conglin.clrpc.transport.router;

import conglin.clrpc.service.ServiceInterface;

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
}
