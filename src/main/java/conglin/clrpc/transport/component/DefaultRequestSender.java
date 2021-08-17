package conglin.clrpc.transport.component;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.Fallback;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.FallbackFailedException;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

/**
 * 默认的请求发送器，采用异步直接发送请求
 */
public class DefaultRequestSender implements RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestSender.class);

    protected final FutureHolder<Long> futureHolder;

    protected final ProviderChooser providerChooser;

    protected final ExecutorService threadPool;

    private final Timer timer;

    // 初始重试后的 threshold
    private final int INITIAL_THRESHOLD;
    // 检查周期
    private final int CHECK_PERIOD;

    public DefaultRequestSender(RpcContext context) {
        this.futureHolder = context.getWith(RpcContextEnum.FUTURE_HOLDER);
        this.providerChooser = context.getWith(RpcContextEnum.PROVIDER_CHOOSER);
        this.threadPool = context.getWith(RpcContextEnum.EXECUTOR_SERVICE);
        PropertyConfigurer c = context.getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        this.CHECK_PERIOD = c.getOrDefault("consumer.retry.check-period", 3000);
        this.INITIAL_THRESHOLD = c.getOrDefault("consumer.retry.initial-threshold", 3000);
        this.timer = CHECK_PERIOD > 0 ? checkFuture() : null;
    }

    @Override
    public RpcFuture sendRequest(BasicRequest request, Fallback fallbackObject, String remoteAddress) {
        RpcFuture future = putFuture(request);
        future.fallback(fallbackObject);
        doSendRequest(request, remoteAddress);
        return future;
    }

    @Override
    public void resendRequest(BasicRequest request, String remoteAddress) {
        doSendRequest(request, remoteAddress);
    }

    @Override
    public void shutdown() {
        if (timer != null) {
            timer.cancel();
        }
    }

    /**
     * 保存Future
     * 
     * @param request
     * @return
     */
    protected RpcFuture putFuture(BasicRequest request) {
        RpcFuture future = new BasicFuture(request);
        futureHolder.putFuture(future.identifier(), future);
        return future;
    }

    /**
     * 发送请求
     * 
     * @param request
     * @param targetAddress
     */
    protected void doSendRequest(BasicRequest request, String targetAddress) {
        String serviceName = request.serviceName();
        threadPool.execute(() -> {
            if (targetAddress == null) {
                providerChooser.choose(request).pipeline().fireChannelRead(request);
            } else {
                providerChooser.choose(serviceName, serviceInstance -> targetAddress.equals(serviceInstance.address())).pipeline().fireChannelRead(request);
            }
        });
    }

    /**
     * 轮询线程，检查超时 RpcFuture 超时重试
     */
    private Timer checkFuture() {
        Timer timer = new Timer("check-uncomplete-future", true);
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
                        BasicRequest r = f.request();
                        Fallback fallback = f.fallback();
                        if(fallback.needFallback(retryTimes)) {
                            iterator.remove();
                            BasicResponse fallbackResponse = null;
                            try {
                                Object fallbackResult = fallback.fallback(r.methodName(), r.parameters());
                                fallbackResponse = new BasicResponse(r.messageId(), fallbackResult);
                            } catch (FallbackFailedException e) {
                                LOGGER.warn("Request(id={}) Fallback Failed. Cause: {}", r.messageId(), e.getCause());
                                fallbackResponse = new BasicResponse(r.messageId(), true, e);
                            }
                            f.fallbackDone(fallbackResponse);
                        } else {
                            resendRequest(r);
                            LOGGER.warn("Service response(messageId={}) is too slow. Retry (times={})...",
                                    r.messageId(), retryTimes);
                        }
                    }
                }
            }
        }, INITIAL_THRESHOLD, CHECK_PERIOD);
        return timer;
    }
}