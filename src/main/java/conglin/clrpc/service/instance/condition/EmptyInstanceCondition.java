package conglin.clrpc.service.instance.condition;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.instance.ServiceInstance;

final public class EmptyInstanceCondition implements InstanceCondition {

    @Override
    public boolean test(ServiceInstance t) {
        return true;
    }

    @Override
    public void bindServiceInterface(ServiceInterface<?> serviceInterface) {
        
    }
    
}
