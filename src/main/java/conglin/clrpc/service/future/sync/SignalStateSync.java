package conglin.clrpc.service.future.sync;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class SignalStateSync implements StateSync {

    protected final AtomicInteger state;

    public SignalStateSync() {
        state = new AtomicInteger(PENDING);
    }

    @Override
    public void await() throws InterruptedException {
        if (isPending()) {
            synchronized (this) {
                wait();
            }
        }
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        if (isPending()) {
            synchronized (this) {
                wait(unit.toMillis(timeout));
            }
            return !isPending();
        }
        return true;
    }

    @Override
    public void signal() {
        if(!isPending()) return;
        synchronized (this) {
            if(isPending()) {
                notifyAll();
            }
        }
    }

    @Override
    public int state() {
        return state.get();
    }

    @Override
    public void state(int value) {
        state.set(value);
    }

    @Override
    public boolean casState(int expect, int update) {
        return state.compareAndSet(expect, update);
    }
}
