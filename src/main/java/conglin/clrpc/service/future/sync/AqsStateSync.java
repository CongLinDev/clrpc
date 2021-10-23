package conglin.clrpc.service.future.sync;

import java.io.Serial;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * 基于 {@link java.util.concurrent.locks.AbstractQueuedSynchronizer} 的同步器
 */
public class AqsStateSync extends AbstractQueuedSynchronizer implements StateSync {

    @Serial
    private static final long serialVersionUID = -7504883045517282600L;

    @Override
    public void await() throws InterruptedException {
        super.acquire(0);
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return super.tryAcquireNanos(0, unit.toNanos(timeout));
    }

    @Override
    public void signal() {
        super.release(0);
    }

    @Override
    public int state() {
        return getState();
    }

    @Override
    public void state(int value) {
        setState(value);
    }

    @Override
    public boolean casState(int expect, int update) {
        return compareAndSetState(expect, update);
    }

    @Override
    protected boolean tryAcquire(int arg) {
        return !isPending();
    }

    @Override
    protected boolean tryRelease(int arg) {
        if (isPending()) {
            throw new UnsupportedOperationException();
        }
        return true;
    }
}