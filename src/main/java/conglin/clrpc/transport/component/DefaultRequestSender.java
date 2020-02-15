package conglin.clrpc.transport.component;

import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicRequest;

public class DefaultRequestSender implements RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultRequestSender.class);

    protected final FuturesHolder<Long> futuresHolder;

    protected final ProviderChooser providerChooser;

    protected final ExecutorService threadPool;

    public DefaultRequestSender(FuturesHolder<Long> futuresHolder, ProviderChooser providerChooser,
            ExecutorService threadPool) {
        this.futuresHolder = futuresHolder;
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
                providerChooser.choose(serviceName, request).pipeline().fireChannelRead(request);
            } else {
                providerChooser.choose(serviceName, targetAddress).pipeline().fireChannelRead(request);
            }
        });
    }

    /**
     * 轮询线程，检查超时 RpcFuture 超时重试
     */
    private void checkFuture() {
        final long MAX_DELARY = 3000; // 最大延迟为3000 ms
        new Timer("check-uncomplete-future", true).schedule(new TimerTask() {

            @Override
            public void run() {
                Iterator<RpcFuture> iterator = futuresHolder.iterator();
                while (iterator.hasNext()) {
                    BasicFuture f = (BasicFuture) iterator.next();
                    if (f.isPending() && f.timeout()) {
                        resendRequest(f.request());
                        f.retry();
                        LOGGER.warn("Service response(requestId={}) is too slow. Retry...", f.identifier());
                    }
                }
            }
        }, MAX_DELARY);
    }
}