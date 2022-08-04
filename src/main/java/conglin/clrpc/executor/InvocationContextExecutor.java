package conglin.clrpc.executor;

import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.executor.pipeline.CommonChainExecutor;
import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.identifier.IdentifierGenerator;
import conglin.clrpc.invocation.message.Message;
import conglin.clrpc.invocation.message.Payload;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.invocation.strategy.FailStrategy;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.Destroyable;
import conglin.clrpc.lifecycle.Initializable;

public class InvocationContextExecutor extends CommonChainExecutor implements Initializable, Destroyable {
    private static final Logger LOGGER = LoggerFactory.getLogger(InvocationContextExecutor.class);

    private final Map<Long, InvocationContext> contexts;
    private ScheduledExecutorService scheduledExecutorService;
    protected IdentifierGenerator identifierGenerator;

    public InvocationContextExecutor() {
        contexts = new ConcurrentHashMap<>();
    }

    @Override
    public int order() {
        return 2;
    }

    @Override
    public void init() {
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        long checkPeriod = Integer.parseInt(properties.getProperty("invocation.retry.check-period", "3000"));
        long initialThreshold = Integer.parseInt(properties.getProperty("invocation.retry.initial-threshold", "3000"));

        if (checkPeriod > 0) {
            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1, runnable -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                t.setName("check-uncompleted-future");
                return t;
            });
            this.scheduledExecutorService.scheduleWithFixedDelay(() -> {
                LOGGER.debug("check uncompleted future task is beginning");
                Iterator<InvocationContext> iterator = contexts.values().iterator();
                while (iterator.hasNext()) {
                    InvocationContext invocationContext = iterator.next();
                    if (!invocationContext.getFuture().isPending()) {
                        iterator.remove();
                        continue;
                    }

                    if (!invocationContext.isTimeout()) {
                        continue;
                    }

                    FailStrategy failStrategy = invocationContext.getFailStrategy();
                    failStrategy.timeout(invocationContext);
                    if (!invocationContext.getFuture().isPending()) {
                        iterator.remove();
                        continue;
                    }

                    InvocationContextExecutor.this.nextOutbound(invocationContext); // retry
                    LOGGER.warn("Service response(identifier={}) is too slow. Retry...",
                            invocationContext.getIdentifier());
                }
                LOGGER.debug("check uncompleted future task is done");
            }, initialThreshold, checkPeriod, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void inbound(Object object) {
        if (object instanceof Message message) {
            execute(message);
        } else {
            nextInbound(object);
        }
    }

    /**
     * 处理 {@link ResponsePayload}
     * 
     * @param message
     */
    protected void execute(Message message) {
        Payload payload = message.payload();
        if (!(payload instanceof ResponsePayload)) {
            nextInbound(message);
        }
        Long messageId = message.messageId();
        LOGGER.debug("Receive response (messageId={})", messageId);
        InvocationContext invocationContext = contexts.remove(messageId);
        if (invocationContext == null) {
            LOGGER.error("Can not find binding invocationContext (messageId={})", messageId);
            return;
        }

        if (invocationContext.getFuture().isPending()) {
            ResponsePayload response = (ResponsePayload) payload;
            if (response.isError()) {
                invocationContext.getFailStrategy().error(invocationContext, response);
            } else {
                invocationContext.setResponse(response);
            }
        } else {
            LOGGER.error("Can not find binding invocationContext (messageId={})", messageId);
        }
    }


    @Override
    public void outbound(Object object) {
        if (object instanceof InvocationContext invocationContext) {
            Long messageId = identifierGenerator.generate();
            invocationContext.setIdentifier(messageId);
            contexts.put(messageId, invocationContext);
            nextOutbound(invocationContext);
        } else {
            nextOutbound(object);
        }
    }

    @Override
    public void destroy() {
        waitForUncompletedInvocation();
        if (!this.scheduledExecutorService.isShutdown()) {
            this.scheduledExecutorService.shutdown();
        }
    }

    /**
     * 等待未完成的 {@link InvocationContext}
     */
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
        Iterator<InvocationContext> iterator = contexts.values().iterator();
        while (iterator.hasNext()) {
            InvocationContext context = iterator.next();
            if (!context.getFuture().isPending()) {
                iterator.remove();
            }
        }
    }
}
