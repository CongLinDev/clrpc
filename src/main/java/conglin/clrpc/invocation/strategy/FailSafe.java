package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.invocation.message.AtomicResponsePayload;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;

final public class FailSafe implements FailStrategy {
    private static final AtomicResponsePayload RESPONSE = new AtomicResponsePayload(false, null);

    @Override
    public void noTarget(InvocationContext context, NoAvailableServiceInstancesException exception) {
        context.setResponse(RESPONSE);
    }

    @Override
    public void timeout(InvocationContext context) {
        context.setResponse(RESPONSE);
    }

    @Override
    public void limit(InvocationContext context) {
        context.setResponse(RESPONSE);
    }

    @Override
    public void error(InvocationContext context, ResponsePayload sourcePayload) {
        context.setResponse(RESPONSE);
    }
}
