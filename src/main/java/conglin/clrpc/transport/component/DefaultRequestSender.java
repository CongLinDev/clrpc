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
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.service.future.strategy.FailFast;
import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.RequestPayload;
import conglin.clrpc.transport.message.RequestWrapper;
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

    protected FutureHolder<Long> futureHolder;

    protected Router router;

    private Timer timer;

    // 初始重试后的 threshold
    private int INITIAL_THRESHOLD;
    // 检查周期
    private int CHECK_PERIOD;

    @Override
    public void init() {
        this.identifierGenerator = getContext().getWith(ComponentContextEnum.IDENTIFIER_GENERATOR);
        this.futureHolder = getContext().getWith(ComponentContextEnum.FUTURE_HOLDER);
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
    public InvocationFuture sendRequest(RequestWrapper requestWrapper) {
        Long messageId = identifierGenerator.generate();
        InvocationFuture future = putFuture(messageId, requestWrapper.getRequest());
        Class<? extends FailStrategy> failStrategyClass = requestWrapper.getFailStrategyClass();
        future.failStrategy(failStrategyClass == null ? FailFast.class : failStrategyClass);
        try {
            doSendRequest(messageId, requestWrapper);
        } catch (NoAvailableServiceInstancesException e) {
            if (!future.failStrategy().noTarget(e) && future.isPending()) {
                futureHolder.removeFuture(messageId);
            }
        }
        return future;
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
     * 保存Future
     *
     * @param messageId
     * @param request
     * @return
     */
    protected InvocationFuture putFuture(Long messageId, RequestPayload request) {
        InvocationFuture future = new BasicFuture(messageId, request);
        futureHolder.putFuture(future.identifier(), future);
        return future;
    }

    /**
     * 发送请求
     *
     * @param messageId
     * @param requestWrapper
     * @throws NoAvailableServiceInstancesException
     */
    protected void doSendRequest(Long messageId, RequestWrapper requestWrapper)
            throws NoAvailableServiceInstancesException {
        RouterCondition condition = new RouterCondition();
        condition.setServiceName(requestWrapper.getRequest().serviceName());
        condition.setRandom(System.identityHashCode(requestWrapper.getRequest()));
        condition.setInstanceCondition(requestWrapper.getInstanceCondition());
        RouterResult routerResult = router.choose(condition);
        if (requestWrapper.getInstanceConsumer() != null) {
            requestWrapper.getInstanceConsumer().accept(routerResult.getInstance());
        }
        routerResult.send(new Message(messageId, requestWrapper.getRequest()));
    }

    private Timer checkFuture() {
        Timer timer = new Timer("check-uncompleted-future", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Iterator<InvocationFuture> iterator = futureHolder.iterator();
                while (iterator.hasNext()) {
                    InvocationFuture future = iterator.next();
                    if (!future.isPending()) {
                        iterator.remove();
                        continue;
                    }

                    FailStrategy failStrategy = future.failStrategy();
                    if (!failStrategy.isTimeout())
                        continue;
                    
                    if (!failStrategy.timeout() && future.isPending()) {
                        iterator.remove();
                        continue;
                    }

                     // retry
                    try {
                        RequestWrapper wrapper = new RequestWrapper();
                        wrapper.setRequest(((BasicFuture) future).request());
                        doSendRequest(future.identifier(), wrapper); // retry
                        LOGGER.warn("Service response(futureIdentifier={}) is too slow. Retry...",
                                future.identifier());
                    } catch (NoAvailableServiceInstancesException e) {
                        if (!failStrategy.noTarget(e) && future.isPending()) {
                            iterator.remove();
                        }
                    }
                }
            }
        }, INITIAL_THRESHOLD, CHECK_PERIOD);
        return timer;
    }
}