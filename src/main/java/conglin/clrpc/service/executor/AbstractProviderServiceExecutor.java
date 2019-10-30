package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 服务提供者的服务执行器
 */
abstract public class AbstractProviderServiceExecutor implements ServiceExecutor<BasicRequest> {

    private static final Logger log = LoggerFactory.getLogger(AbstractProviderServiceExecutor.class);

    private final ExecutorService executor;

    private final CacheManager<BasicRequest, BasicResponse> cacheManager;

    private Channel channel;

    public AbstractProviderServiceExecutor(ExecutorService executor) {
        this(executor, null);
    }

    public AbstractProviderServiceExecutor(ExecutorService executor,
                    CacheManager<BasicRequest, BasicResponse> cacheManager){
        this.executor = executor;
        this.cacheManager = cacheManager;
    }


    /**
     * 处理请求
     * @param request
     * @param response
     * @return 请求处理是否完成
     * @throws UnsupportedServiceException
     * @throws ServiceExecutionException
     */
    abstract protected boolean doExecute(BasicRequest request, BasicResponse response)
        throws UnsupportedServiceException, ServiceExecutionException;

    @Override
    public void execute(BasicRequest t) {
        log.debug("Receive request " + t.getRequestId());
        
        executor.submit(()->{
            BasicResponse response = null;
            boolean executeCompletely = false;
            if ((response = getCache(t)) == null){
                response = new BasicResponse();
                response.setRequestId(t.getRequestId());

                try{
                    executeCompletely = doExecute(t, response);

                    // save result
                    if(executeCompletely) putCache(t, response);

                }catch(UnsupportedServiceException | ServiceExecutionException e){
                    log.error("Request failed: " + e.getMessage());
                    response.signError();
                    response.setResult(e);

                    executeCompletely = true;
                }
            } else { // fetch from cache
                executeCompletely = true;
            }
            if(executeCompletely) sendResponse(response);
        });
    }

    /**
     * 绑定通道
     * @param channel
     */
    public void bindChannel(Channel channel){
        this.channel = channel;
    }

    /**
     * 检查缓存中是否有需要的结果
     * 
     * @param request
     * @return
     */
    protected BasicResponse getCache(BasicRequest request) {
        if (cacheManager == null)
            return null;
        log.debug("Fetching cached response. Request id = " + request.getRequestId());
        return cacheManager.get(request);
    }
    
    /**
     * 将可缓存的数据放入缓存
     * 
     * @param request
     * @param response
     */
    protected void putCache(BasicRequest request, BasicResponse response) {
        if (cacheManager == null || !response.canCacheForProvider())
            return;
        log.debug("Caching request and response. Request id = " + request.getRequestId());
        cacheManager.put(request, response);
    }

    /**
     * 发送回复
     * @param response
     */
    protected void sendResponse(BasicResponse response) {
        if(response == null) return;
        channel.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Sending response for request id=" + response.getRequestId());
    }


    @Override
    public ExecutorService getExecutorService() {
        return executor;
    }
}

