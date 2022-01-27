package conglin.clrpc.transport.component;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.Fallback;
import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.exception.FallbackFailedException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.*;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;
import conglin.clrpc.transport.router.Router;
import conglin.clrpc.transport.router.RouterCondition;
import conglin.clrpc.transport.router.RouterResult;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Properties;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 默认的请求发送器，直接发送请求
 */
public class DefaultRequestSender implements RequestSender, ContextAware, Initializable, Destroyable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestSender.class);

    private RpcContext context;

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
        this.identifierGenerator = getContext().getWith(RpcContextEnum.IDENTIFIER_GENERATOR);
        this.futureHolder = getContext().getWith(RpcContextEnum.FUTURE_HOLDER);
        this.router = getContext().getWith(RpcContextEnum.ROUTER);
        Properties properties = getContext().getWith(RpcContextEnum.PROPERTIES);
        this.CHECK_PERIOD = Integer.parseInt(properties.getProperty("consumer.retry.check-period", "3000"));
        this.INITIAL_THRESHOLD = Integer.parseInt(properties.getProperty("consumer.retry.initial-threshold", "3000"));
        this.timer = CHECK_PERIOD > 0 ? checkFuture() : null;
    }

    @Override
    public void setContext(RpcContext context) {
        this.context = context;
    }

    @Override
    public RpcContext getContext() {
        return context;
    }

    @Override
    public RpcFuture sendRequest(RequestWrapper requestWrapper) {
        Long messageId = identifierGenerator.generate();
        RpcFuture future = putFuture(messageId, requestWrapper.getRequest());
        future.fallback(requestWrapper.getFallback());
        doSendRequest(messageId, requestWrapper);
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
    protected RpcFuture putFuture(Long messageId, RequestPayload request) {
        RpcFuture future = new BasicFuture(messageId ,request);
        futureHolder.putFuture(future.identifier(), future);
        return future;
    }

    /**
     * 发送请求
     *
     * @param messageId
     * @param requestWrapper
     */
    protected void doSendRequest(Long messageId, RequestWrapper requestWrapper) {
        RouterCondition condition = new RouterCondition();
        condition.setServiceName(requestWrapper.getRequest().serviceName());
        condition.setRandom(System.identityHashCode(requestWrapper.getRequest()));
        condition.setRetryTimes(5);
        condition.setPredicate(requestWrapper.getPredicate());
        try {
            RouterResult routerResult = router.choose(condition);
            if (requestWrapper.getBeforeSendRequest() != null) {
                requestWrapper.getBeforeSendRequest().accept(routerResult.getInstance());
            }
            routerResult.send(new Message(messageId, requestWrapper.getRequest()));
        } catch (NoAvailableServiceInstancesException e) {
            // do nothing wait fallback
        }
        
    }

    /**
     * 轮询线程，检查超时 RpcFuture 超时重试
     */
    private Timer checkFuture() {
        Timer timer = new Timer("check-uncompleted-future", true);
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Iterator<RpcFuture> iterator = futureHolder.iterator();
                while (iterator.hasNext()) {
                    BasicFuture f = (BasicFuture) iterator.next();
                    if (f.isCancelled()) {
                        iterator.remove();
                        continue;
                    }

                    int retryTimes = f.retryTimes() + 1;
                    if (f.timeout(INITIAL_THRESHOLD << (retryTimes - 1)) && f.retry()) {
                        RequestPayload r = f.request();
                        Fallback fallback = f.fallback();
                        if (fallback != null && fallback.needFallback(retryTimes)) {
                            iterator.remove();
                            ResponsePayload fallbackResponse = null;
                            try {
                                Object fallbackResult = fallback.fallback(r.methodName(), r.parameters());
                                fallbackResponse = new ResponsePayload(fallbackResult);
                            } catch (FallbackFailedException e) {
                                LOGGER.warn("Future(id={}) Fallback Failed. Cause: {}", f.identifier(), e.getCause());
                                fallbackResponse = new ResponsePayload(true, e);
                            }
                            f.fallbackDone(fallbackResponse);
                        } else {
                            RequestWrapper wrapper = new RequestWrapper();
                            wrapper.setRequest(r);
                            doSendRequest(f.identifier(), wrapper); // retry
                            LOGGER.warn("Service response(futureIdentifier={}) is too slow. Retry (times={})...",
                                    f.identifier(), retryTimes);
                        }
                    }
                }
            }
        }, INITIAL_THRESHOLD, CHECK_PERIOD);
        return timer;
    }
}