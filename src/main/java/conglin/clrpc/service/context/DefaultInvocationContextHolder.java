package conglin.clrpc.service.context;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultInvocationContextHolder implements InvocationContextHolder<Long> {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInvocationContextHolder.class);

    private final Map<Long, InvocationContext> contexts;

    public DefaultInvocationContextHolder() {
        contexts = new ConcurrentHashMap<>();
    }

    @Override
    public void put(Long key, InvocationContext context) {
        contexts.put(key, context);
    }

    @Override
    public InvocationContext get(Long key) {
        return contexts.get(key);
    }

    @Override
    public InvocationContext remove(Long key) {
        return contexts.remove(key);
    }

    @Override
    public Iterator<InvocationContext> iterator() {
        return contexts.values().iterator();
    }

    @Override
    public void waitForUncompletedInvocation() {
        if (!contexts.isEmpty()) {
            clearCompleted(); // help clear completed.
        }

        while (!contexts.isEmpty()) {
            try {
                LOGGER.info("Waiting uncompleted context for 500 ms.");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }

    
    /**
     * 尽最大可能清空 已经完成的  {@link InvocationContext}
     */
    protected void clearCompleted() {
        Iterator<InvocationContext> iterator = iterator();
        while (iterator.hasNext()) {
            InvocationContext context = iterator.next();
            if (!context.getFuture().isPending()) {
                iterator.remove();
            }
        }
    }
}
