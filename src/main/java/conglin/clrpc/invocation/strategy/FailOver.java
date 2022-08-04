package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;

final public class FailOver implements FailStrategy {

    @Override
    public void noTarget(InvocationContext context, NoAvailableServiceInstancesException exception) {

    }

    @Override
    public void timeout(InvocationContext context) {

    }

    @Override
    public void limit(InvocationContext context) {

    }

    @Override
    public void error(InvocationContext context, ResponsePayload sourcePayload) {

    }

}
