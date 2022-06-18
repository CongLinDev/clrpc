package conglin.clrpc.service.instance.condition;

import java.util.function.Predicate;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.instance.ServiceInstance;

public interface InstanceCondition extends Predicate<ServiceInstance> {

    /**
     * 绑定服务接口
     * 
     * @param serviceInterface
     */
    void bindServiceInterface(ServiceInterface<?> serviceInterface);
}
