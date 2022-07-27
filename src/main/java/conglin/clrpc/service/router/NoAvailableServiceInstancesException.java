package conglin.clrpc.service.router;

import conglin.clrpc.common.exception.ServiceException;

public class NoAvailableServiceInstancesException extends ServiceException {

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
