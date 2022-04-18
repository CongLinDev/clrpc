package conglin.clrpc.transport.component;

import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.DestroyFailedException;
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
public class DefaultRequestSender implements RequestSender, ComponentContextAware, Initializable, Destroyable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestSender.class);

    private ComponentContext context;

    protected IdentifierGenerator identifierGenerator;

    protected InvocationContextHolder<Long> contextHolder;

    protected Router router;

    private Timer timer;

    // 初始重试后的 threshold
    private int INITIAL_THRESHOLD;
    // 检查周期
    private int CHECK_PERIOD;

    @Override
    public void init() {
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
        this.contextHolder = getContext().getWith(ComponentContextEnum.INVOCATION_CONTEXT_HOLDER);
        this.router = getContext().getWith(ComponentContextEnum.ROUTER);
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        this.CHECK_PERIOD = Integer.parseInt(properties.getProperty("consumer.retry.check-period", "3000"));
        this.INITIAL_THRESHOLD = Integer.parseInt(properties.getProperty("consumer.retry.initial-threshold", "3000"));
        this.timer = CHECK_PERIOD > 0 ? checkFuture() : null;
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
    public void send(InvocationContext invocationContext) {
        Long messageId = identifierGenerator.generate();
        invocationContext.setIdentifier(messageId);
        contextHolder.put(messageId, invocationContext);
        try {
            doSend(invocationContext);
        } catch (NoAvailableServiceInstancesException e) {
            if (!invocationContext.getFailStrategy().noTarget(invocationContext, e)
                    && !invocationContext.getFuture().isPending()) {
                contextHolder.remove(messageId);
            }
        }
    }

    @Override
    public boolean isDestroyed() {
        return timer != null;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        timer.cancel();
        timer = null;
    }

    /**
     * 发送请求
     *
     * @param invocationContext
     * @throws NoAvailableServiceInstancesException
     */
    protected void doSend(InvocationContext invocationContext)
            throws NoAvailableServiceInstancesException {
        RouterCondition condition = new RouterCondition();
        condition.setServiceName(invocationContext.getRequest().serviceName());
        condition.setRandom(System.identityHashCode(invocationContext.getRequest()));
        condition.setInstanceCondition(invocationContext.getInstanceCondition());
        RouterResult routerResult = router.choose(condition);
        if (invocationContext.getInstanceConsumer() != null) {
            invocationContext.getInstanceConsumer().accept(routerResult.getInstance());
        }
        routerResult.send(new Message(invocationContext.getIdentifier(), invocationContext.getRequest()));
        LOGGER.debug("Send request for messageId={}", invocationContext.getIdentifier());
    }

    private Timer checkFuture() {
        Timer timer = new Timer("check-uncompleted-future", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
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
                        doSend(invocationContext); // retry
                        LOGGER.warn("Service response(identifier={}) is too slow. Retry...",
                                invocationContext.getIdentifier());
                    } catch (NoAvailableServiceInstancesException e) {
                        if (!failStrategy.noTarget(invocationContext, e)
                                && !invocationContext.getFuture().isPending()) {
                            iterator.remove();
                        }
                    }
                }
            }
        }, INITIAL_THRESHOLD, CHECK_PERIOD);
        return timer;
    }
}