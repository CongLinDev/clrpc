package conglin.clrpc.transport.router;

import conglin.clrpc.common.exception.RpcServiceException;

public class NoAvailableServiceInstancesException extends RpcServiceException {

    private final RouterCondition condition;
    /**
     * 不可用
     */
    public NoAvailableServiceInstancesException(RouterCondition condition) {
        this.condition = condition;
    }

    public RouterCondition getCondition() {
        return condition;
    }
}
