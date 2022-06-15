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
import conglin.clrpc.service.context.DefaultInvocationContextHolder;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.context.InvocationContextHolder;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.service.strategy.FailStrategy;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;
import conglin.clrpc.transport.router.Router;
import conglin.clrpc.transport.router.RouterCondition;
import conglin.clrpc.transport.router.RouterResult;

/**
 * 默认的请求处理器
 * 
 * 负责请求发送与回复的处理
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
        this.contextHolder = new DefaultInvocationContextHolder();
        ObjectLifecycleUtils.assemble(contextHolder, getContext());
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
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

                    doExecute(invocationContext); // retry
                    LOGGER.warn("Service response(identifier={}) is too slow. Retry...",
                            invocationContext.getIdentifier());
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
        doExecute(invocationContext);
    }

    @Override
    public void destroy() {
        contextHolder.waitForUncompletedInvocation();
        if (!this.scheduledExecutorService.isShutdown()) {
            this.scheduledExecutorService.shutdown();
        }
        ObjectLifecycleUtils.destroy(contextHolder);
    }

    /**
     * 发送请求
     *
     * @param invocationContext
     */
    protected void doExecute(InvocationContext invocationContext) {
        RouterCondition condition = new RouterCondition();
        condition.setServiceName(invocationContext.getRequest().serviceName());
        condition.setRandom(invocationContext.getExecuteTimes() ^ invocationContext.getIdentifier().intValue());
        condition.setInstanceCondition(invocationContext.getChoosedInstanceCondition());
        try {
            RouterResult routerResult = router.choose(condition);
            if (invocationContext.getChoosedInstancePostProcessor() != null) {
                invocationContext.getChoosedInstancePostProcessor().accept(routerResult.getInstance());
            }
            invocationContext.increaseExecuteTimes();
            routerResult.execute(new Message(invocationContext.getIdentifier(), invocationContext.getRequest()));
            LOGGER.debug("Execute request for messageId={}", invocationContext.getIdentifier());
        } catch (NoAvailableServiceInstancesException e) {
            if (!invocationContext.getFailStrategy().noTarget(invocationContext, e)
                    && !invocationContext.getFuture().isPending()) {
                contextHolder.remove(invocationContext.getIdentifier());
            }
        }
    }

    @Override
    public void receive(Message message) {
        Payload payload = message.payload();
        if (!(payload instanceof ResponsePayload)) {
            return;
        }
        Long messageId = message.messageId();
        LOGGER.debug("Receive response (messageId={})", messageId);
        InvocationContext invocationContext = contextHolder.remove(messageId);
        if (invocationContext == null) {
            LOGGER.error("Can not find binding invocationContext (messageId={})", messageId);
            return;
        }

        InvocationFuture future = invocationContext.getFuture();
        if (future.isPending()) {
            ResponsePayload response = (ResponsePayload) payload;
            if (response.isError()) {
                invocationContext.getFailStrategy().error(invocationContext, payload);
            } else {
                invocationContext.setResponse(response);
            }
        } else {
            LOGGER.error("Can not find binding invocationContext (messageId={})", messageId);
        }
    }
}