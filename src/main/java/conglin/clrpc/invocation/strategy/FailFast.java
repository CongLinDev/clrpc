package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.ServiceTimeoutException;
import conglin.clrpc.invocation.message.Payload;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;

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
