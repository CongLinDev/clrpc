package conglin.clrpc.transport.component;

import java.util.Iterator;
import java.util.Properties;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.context.InvocationContextHolder;
import conglin.clrpc.service.strategy.FailStrategy;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;
import conglin.clrpc.transport.router.Router;
import conglin.clrpc.transport.router.RouterCondition;
import conglin.clrpc.transport.router.RouterResult;

/**
 * 默认的请求发送器，直接发送请求
 */
public class DefaultInvocationExecutor implements InvocationExecutor, ComponentContextAware, Initializable, Destroyable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultInvocationExecutor.class);

    private ComponentContext context;

    protected IdentifierGenerator identifierGenerator;

    protected InvocationContextHolder<Long> contextHolder;

    protected Router router;

    private ScheduledExecutorService scheduledExecutorService;

    @Override
    public void init() {
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
        this.contextHolder = getContext().getWith(ComponentContextEnum.INVOCATION_CONTEXT_HOLDER);
        this.router = getContext().getWith(ComponentContextEnum.ROUTER);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        long checkPeriod = Integer.parseInt(properties.getProperty("consumer.retry.check-period", "3000"));
        long initialThreshold = Integer.parseInt(properties.getProperty("consumer.retry.initial-threshold", "3000"));

        if (checkPeriod > 0) {
            this.scheduledExecutorService = new ScheduledThreadPoolExecutor(1, runnable -> {
                Thread t = new Thread(runnable);
                t.setDaemon(true);
                t.setName("check-uncompleted-future");
                return t;
            });
            this.scheduledExecutorService.scheduleWithFixedDelay(() -> {
                LOGGER.debug("check uncompleted future task is beginning");
                Iterator<InvocationContext> iterator = contextHolder.iterator();
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
                    if (!failStrategy.timeout(invocationContext) && !invocationContext.getFuture().isPending()) {
                        iterator.remove();
                        continue;
                    }

                    // retry
                    try {
                        doExecute(invocationContext); // retry
                        LOGGER.warn("Service response(identifier={}) is too slow. Retry...",
                                invocationContext.getIdentifier());
                    } catch (NoAvailableServiceInstancesException e) {
                        if (!failStrategy.noTarget(invocationContext, e)
                                && !invocationContext.getFuture().isPending()) {
                            iterator.remove();
                        }
                    } catch (Exception e) {
                        LOGGER.warn(
                                "Service response(identifier={}) is too slow. but we catch an exception and we will do nothing",
                                invocationContext.getIdentifier(), e);
                    }
                }
                LOGGER.debug("check uncompleted future task is done");
            }, initialThreshold, checkPeriod, TimeUnit.MILLISECONDS);
        }
    }

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    public void execute(InvocationContext invocationContext) {
        Long messageId = identifierGenerator.generate();
        invocationContext.setIdentifier(messageId);
        contextHolder.put(messageId, invocationContext);
        try {
            doExecute(invocationContext);
        } catch (NoAvailableServiceInstancesException e) {
            if (!invocationContext.getFailStrategy().noTarget(invocationContext, e)
                    && !invocationContext.getFuture().isPending()) {
                contextHolder.remove(messageId);
            }
        } catch (Exception e) {
            LOGGER.warn(
                    "we catch an exception and we will do nothing",
                    invocationContext.getIdentifier(), e);
        }
    }

    @Override
    public void destroy() {
        if (this.scheduledExecutorService.isShutdown()) {
            this.scheduledExecutorService.shutdown();
        }
    }

    /**
     * 发送请求
     *
     * @param invocationContext
     * @throws NoAvailableServiceInstancesException
     */
    protected void doExecute(InvocationContext invocationContext)
            throws NoAvailableServiceInstancesException {
        RouterCondition condition = new RouterCondition();
        condition.setServiceName(invocationContext.getRequest().serviceName());
        condition.setRandom(invocationContext.getExecuteTimes() ^ invocationContext.getIdentifier().intValue());
        condition.setInstanceCondition(invocationContext.getChoosedInstanceCondition());
        RouterResult routerResult = router.choose(condition);
        if (invocationContext.getChoosedInstancePostProcessor() != null) {
            invocationContext.getChoosedInstancePostProcessor().accept(routerResult.getInstance());
        }
        invocationContext.increaseExecuteTimes();
        routerResult.execute(new Message(invocationContext.getIdentifier(), invocationContext.getRequest()));
        LOGGER.debug("Execute request for messageId={}", invocationContext.getIdentifier());
    }
}