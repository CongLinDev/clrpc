package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.message.Payload;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;

final public class FailSafe implements FailStrategy {
    private static final ResponsePayload RESPONSE = new ResponsePayload(false, null);

    @Override
    public boolean noTarget(InvocationContext context, NoAvailableServiceInstancesException exception) {
        context.setResponse(RESPONSE);
        return false;
    }

    @Override
    public boolean timeout(InvocationContext context) {
        context.setResponse(RESPONSE);
        return false;
    }

    @Override
    public boolean limit(InvocationContext context) {
        context.setResponse(RESPONSE);
        return false;
    }

    @Override
    public boolean error(InvocationContext context, Payload sourcePayload) {
        context.setResponse(RESPONSE);
        return false;
    }
    
}
