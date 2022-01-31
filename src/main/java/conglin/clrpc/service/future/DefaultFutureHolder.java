package conglin.clrpc.service.future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultFutureHolder implements FutureHolder<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFutureHolder.class);

    private final Map<Long, RpcFuture> rpcFutures;

    public DefaultFutureHolder() {
        rpcFutures = new ConcurrentHashMap<>();
    }

    @Override
    public void putFuture(Long key, RpcFuture rpcFuture) {
        rpcFutures.put(key, rpcFuture);
    }

    @Override
    public RpcFuture getFuture(Long key) {
        return rpcFutures.get(key);
    }

    @Override
    public RpcFuture removeFuture(Long key) {
        return rpcFutures.remove(key);
    }

    @Override
    public Iterator<RpcFuture> iterator() {
        return rpcFutures.values().iterator();
    }

    @Override
    public void waitForUncompletedFuture() {
        if (!rpcFutures.isEmpty()) {
            clearCompletedFuture(); // help clear future completed.
        }

        while (!rpcFutures.isEmpty()) {
            try {
                LOGGER.info("Waiting uncompleted futures for 500 ms.");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    
    /**
     * 尽最大可能清空 已经完成的  {@link conglin.clrpc.service.future.RpcFuture}
     */
    protected void clearCompletedFuture() {
        Iterator<RpcFuture> iterator = iterator();
        while (iterator.hasNext()) {
            RpcFuture future = iterator.next();
            if (!future.isPending()) {
                iterator.remove();
            }
        }
    }
}