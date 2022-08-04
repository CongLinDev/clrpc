package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.ServiceTimeoutException;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.invocation.message.AtomicResponsePayload;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;

/**
 * fail fast
 */
final public class FailFast implements FailStrategy {

    @Override
    public void noTarget(InvocationContext context, NoAvailableServiceInstancesException exception) {
        context.setResponse(new AtomicResponsePayload(true, exception));
    }

    @Override
    public void timeout(InvocationContext context) {
        context.setResponse(new AtomicResponsePayload(true, new ServiceTimeoutException()));
    }

    @Override
    public void limit(InvocationContext context) {
        context.setResponse(new AtomicResponsePayload(true, new ServiceTimeoutException()));
    }

    @Override
    public void error(InvocationContext context, ResponsePayload sourcePayload) {
        context.setResponse(sourcePayload);
    }
}
