package conglin.clrpc.transport.component;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.fallback.FallbackFailedException;
import conglin.clrpc.service.fallback.FallbackHolder;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicRequest;

/**
 * 默认的请求发送器，采用异步直接发送请求
 */
public class DefaultRequestSender implements RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestSender.class);

    protected final FuturesHolder<Long> futuresHolder;

    protected final FallbackHolder fallbackHolder;

    protected final ProviderChooser providerChooser;

    protected final ExecutorService threadPool;

    public DefaultRequestSender(FuturesHolder<Long> futuresHolder, FallbackHolder fallbackHolder,
            ProviderChooser providerChooser, ExecutorService threadPool) {
        this.futuresHolder = futuresHolder;
        this.fallbackHolder = fallbackHolder;
        this.providerChooser = providerChooser;
        this.threadPool = threadPool;
        checkFuture();
    }

    @Override
    public RpcFuture sendRequest(BasicRequest request, String remoteAddress) {
        RpcFuture future = putFuture(request);
        doSendRequest(request, remoteAddress);
        return future;
    }

    @Override
    public void resendRequest(BasicRequest request, String remoteAddress) {
        doSendRequest(request, remoteAddress);
    }

    /**
     * 保存Future
     * 
     * @param request
     * @return
     */
    protected RpcFuture putFuture(BasicRequest request) {
        RpcFuture future = new BasicFuture(request);
        futuresHolder.putFuture(future.identifier(), future);
        return future;
    }

    /**
     * 发送请求
     * 
     * @param request
     * @param targetAddress
     */
    protected void doSendRequest(BasicRequest request, String targetAddress) {
        String serviceName = request.getServiceName();
        threadPool.execute(() -> {
            if (targetAddress == null) {
                providerChooser.choose(request).pipeline().fireChannelRead(request);
            } else {
                providerChooser.choose(serviceName, targetAddress).pipeline().fireChannelRead(request);
            }
        });
    }

    /**
     * 轮询线程，检查超时 RpcFuture 超时重试
     */
    private void checkFuture() {
        final long MAX_DELARY = BasicFuture.getTimeThreshold(); // 首次延迟
        final long PERIOD = 3000L; // 执行周期
        new Timer("check-uncomplete-future", true).schedule(new TimerTask() {

            @Override
            public void run() {
                Iterator<RpcFuture> iterator = futuresHolder.iterator();
                while (iterator.hasNext()) {
                    BasicFuture f = (BasicFuture) iterator.next();
                    if (f.timeout() && f.retry()) {
                        int retryTimes = f.retryTimes();
                        BasicRequest r = f.request();
                        if (fallbackHolder.needFallback(retryTimes)) {
                            iterator.remove();
                            try {
                                Object fallbackResult = fallbackHolder.fallback(r.getServiceName(), r.getMethodName(),
                                        r.getParameters());
                                f.done(fallbackResult);
                            } catch (FallbackFailedException e) {
                                LOGGER.warn("Request(id={}) Fallback Failed. Cause: {}", r.getMessageId(),
                                        e.getCause());
                                f.done(e);
                            }
                        } else {
                            resendRequest(r);
                            LOGGER.warn("Service response(messageId={}) is too slow. Retry (times={})...",
                                    r.getMessageId(), retryTimes);
                        }
                    }
                }
            }
        }, MAX_DELARY, PERIOD);
    }
}