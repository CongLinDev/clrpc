package conglin.clrpc.service.strategy;

import conglin.clrpc.common.exception.ServiceTimeoutException;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;

/**
 * fail fast
 */
final public class FailFast implements FailStrategy {

    @Override
    public boolean noTarget(InvocationContext context, NoAvailableServiceInstancesException exception) {
        context.setResponse(new ResponsePayload(true, exception));
        return false;
    }

    @Override
    public boolean timeout(InvocationContext context) {
        context.setResponse(new ResponsePayload(true, new ServiceTimeoutException()));
        return false;
    }

    @Override
    public boolean limit(InvocationContext context) {
        context.setResponse(new ResponsePayload(true, new ServiceTimeoutException()));
        return false;
    }

    @Override
    public boolean error(InvocationContext context, Payload sourcePayload) {
        context.setResponse((ResponsePayload) sourcePayload);
        return false;
    }
}
