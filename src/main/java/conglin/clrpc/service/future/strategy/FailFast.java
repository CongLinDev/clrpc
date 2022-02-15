package conglin.clrpc.service.future.strategy;

import conglin.clrpc.common.exception.ServiceTimeoutException;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;

/**
 * fail fast
 */
final public class FailFast implements FailStrategy {

    private final long latestTime;
    private final long threshold = 5000L;
    private final InvocationFuture future;

    public FailFast(InvocationFuture future) {
        latestTime = System.currentTimeMillis() + threshold;
        this.future = future;
    }

    @Override
    public boolean noTarget(NoAvailableServiceInstancesException exception) {
        future.done(new ResponsePayload(true, exception));
        return false;
    }

    @Override
    public boolean isTimeout() {
        return latestTime < System.currentTimeMillis();
    }

    @Override
    public boolean timeout() {
        future.done(new ResponsePayload(true, new ServiceTimeoutException()));
        return false;
    }

    @Override
    public boolean error(Payload sourcePayload) {
        future.done(sourcePayload);
        return false;
    }
}

