package conglin.clrpc.service.future.strategy;

import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;

final public class FailSafe implements FailStrategy {
    private static final long threshold = 5000L;
    private static final ResponsePayload RESPONSE = new ResponsePayload(false, null);

    @Override
    public boolean noTarget(InvocationContext context, NoAvailableServiceInstancesException exception) {
        context.setResponse(RESPONSE);
        return false;
    }

    @Override
    public boolean isTimeout(InvocationContext context) {
        return context.getInvokeBeginTime() + threshold < System.currentTimeMillis();
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
