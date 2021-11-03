package conglin.clrpc.router;

import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.router.instance.ServiceInstance;

public class NoAvailableServiceInstancesException extends RpcServiceException {

    private final RouterCondition<ServiceInstance> condition;
    /**
     * 不可用
     */
    public NoAvailableServiceInstancesException(RouterCondition<ServiceInstance> condition) {
        this.condition = condition;
    }

    public RouterCondition<ServiceInstance> getCondition() {
        return condition;
    }
}
