package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

public class BasicConsumerServiceExecutor extends AbstractConsumerServiceExecutor {

    private static final Logger log = LoggerFactory.getLogger(BasicConsumerServiceExecutor.class);

    protected final FuturesHolder<Long> futuresHolder;

    protected final BiFunction<String, Object, Channel> providerChooser;

    public BasicConsumerServiceExecutor(ConsumerContext context){
        this(context.getFuturesHolder(), context.getProviderChooser(),
            context.getExecutorService(), context.getCacheManager());
    }

    public BasicConsumerServiceExecutor(FuturesHolder<Long> futuresHolder,
        BiFunction<String, Object, Channel> providerChooser,
        ExecutorService executor, CacheManager<BasicRequest, BasicResponse> cacheManager) {
        super(executor, cacheManager);
        this.futuresHolder = futuresHolder;
        this.providerChooser = providerChooser;
    }

    public BasicConsumerServiceExecutor(FuturesHolder<Long> futuresHolder,
        BiFunction<String, Object, Channel> providerChooser,
        ExecutorService executor) {
        this(futuresHolder, providerChooser, executor, null);
    }

    @Override
    protected void doExecute(BasicResponse response) {
        RpcFuture future = futuresHolder.removeFuture(response.getRequestId());

        if(future != null){
            future.done(response);
        }
    }

    @Override
    protected void doSendRequest(BasicRequest request, Object object) {
        String serviceName = request.getServiceName();
        Channel channel = providerChooser.apply(serviceName, object);

        channel.writeAndFlush(request).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Send request Id = " + request.getRequestId());
    }

    @Override
    protected void doPutFuture(Long key, RpcFuture future) {
        futuresHolder.putFuture(key, future);
    }
}