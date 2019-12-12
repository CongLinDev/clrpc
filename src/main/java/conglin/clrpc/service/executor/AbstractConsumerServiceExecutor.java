package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

abstract public class AbstractConsumerServiceExecutor
    implements ServiceExecutor<BasicResponse>, RequestSender {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractConsumerServiceExecutor.class);

    private final ExecutorService executor;

    private final CacheManager<BasicRequest, BasicResponse> cacheManager;

    public AbstractConsumerServiceExecutor(ExecutorService executor) {
        this(executor, null);
    }

    public AbstractConsumerServiceExecutor(ExecutorService executor,
            CacheManager<BasicRequest, BasicResponse> cacheManager){
        this.executor = executor;
        this.cacheManager = cacheManager;
    }

    @Override
    public void execute(BasicResponse t) {
        Long requestId = t.getRequestId();
        LOGGER.debug("Receive response responseId=" + requestId);
        executor.submit(()->{
            doExecute(t);
        });
    }

    /**
     * 执行具体方法
     * @param response
     */
    abstract protected void doExecute(BasicResponse response);

    @Override
    public RpcFuture sendRequest(BasicRequest request) {
        RpcFuture future = putFuture(request);
        if(future.isDone()) return future;

        // executor.submit(()->{
        //     doSendRequest(request, request.getRequestId());
        // });
        doSendRequest(request, request.getRequestId());
        return future;
    }

    @Override
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) {
        RpcFuture future = putFuture(request);
        if(future.isDone()) return future;

        // executor.submit(()->{
        //     doSendRequest(request, remoteAddress);
        // });
        doSendRequest(request, remoteAddress);
        return future;
    } 

    @Override
    public void resendRequest(BasicRequest request) {
        // executor.submit(()->{
        //     doSendRequest(request, request.getRequestId());
        // });
        doSendRequest(request, request.getRequestId());
    }

    @Override
    public void resendRequest(String remoteAddress, BasicRequest request) {
        // executor.submit(()->{
        //     doSendRequest(request, remoteAddress);
        // });
        doSendRequest(request, remoteAddress);
    }

    /**
     * 发送请求方法
     * 调用 {@link io.netty.channel.Channel#write(Object)}
     * @param request
     * @param object 随机对象
     */
    abstract protected void doSendRequest(BasicRequest request, Object object);

    /**
     * 暂存Future对象
     * @param key
     * @param future
     */
    abstract protected void doPutFuture(Long key, RpcFuture future);

    /**
     * 保存Future对象
     * @param request
     * @return
     */
    protected RpcFuture putFuture(BasicRequest request){
        
        RpcFuture future = new BasicFuture(this, request);

        BasicResponse cachedResponse = null;
        if(cacheManager != null &&
            (cachedResponse = cacheManager.get(request)) != null ){
            LOGGER.debug("Fetching cached response. Request id = " + request.getRequestId());
            future.done(cachedResponse);
            return future;
        }
        doPutFuture(future.identifier(), future);
        return future;
    }
    
    @Override
    public ExecutorService getExecutorService() {
        return executor;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        // do nothing
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

}