package conglin.clrpc.service.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFutureHolder implements FutureHolder<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFutureHolder.class);

    private final Map<Long, InvocationFuture> futures;

    public DefaultFutureHolder() {
        futures = new ConcurrentHashMap<>();
    }

    @Override
    public void putFuture(Long key, InvocationFuture future) {
        futures.put(key, future);
    }

    @Override
    public InvocationFuture getFuture(Long key) {
        return futures.get(key);
    }

    @Override
    public InvocationFuture removeFuture(Long key) {
        return futures.remove(key);
    }

    @Override
    public Iterator<InvocationFuture> iterator() {
        return futures.values().iterator();
    }

    @Override
    public void waitForUncompletedFuture() {
        if (!futures.isEmpty()) {
            clearCompletedFuture(); // help clear future completed.
        }

        while (!futures.isEmpty()) {
            try {
                LOGGER.info("Waiting uncompleted futures for 500 ms.");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    
    /**
     * 尽最大可能清空 已经完成的  {@link conglin.clrpc.service.future.InvocationFuture}
     */
    protected void clearCompletedFuture() {
        Iterator<InvocationFuture> iterator = iterator();
        while (iterator.hasNext()) {
            InvocationFuture future = iterator.next();
            if (!future.isPending()) {
                iterator.remove();
            }
        }
    }
}