package conglin.clrpc.service.strategy;

import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;

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
