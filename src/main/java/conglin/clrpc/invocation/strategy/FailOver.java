package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.message.Payload;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;

final public class FailOver implements FailStrategy {

    @Override
    public boolean noTarget(InvocationContext context, NoAvailableServiceInstancesException exception) {
        return true;
    }

    @Override
    public boolean timeout(InvocationContext context) {
        return true;
    }

    @Override
    public boolean limit(InvocationContext context) {
        return true;
    }

    @Override
    public boolean error(InvocationContext context, Payload sourcePayload) {
        return true;
    }
}
