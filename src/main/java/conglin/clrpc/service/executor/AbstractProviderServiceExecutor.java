package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 服务提供者的服务执行器
 */
abstract public class AbstractProviderServiceExecutor implements ServiceExecutor<BasicRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProviderServiceExecutor.class);

    private final ExecutorService executor;

    private final CacheManager<BasicRequest, BasicResponse> cacheManager;

    private Channel channel;

    public AbstractProviderServiceExecutor(ExecutorService executor) {
        this(executor, null);
    }

    public AbstractProviderServiceExecutor(ExecutorService executor,
            CacheManager<BasicRequest, BasicResponse> cacheManager) {
        this.executor = executor;
        this.cacheManager = cacheManager;
    }

    /**
     * 处理请求
     * 
     * @param request
     * @return 返回回复消息
     * @throws UnsupportedServiceException 此Provider不支持该服务时抛出
     * @throws ServiceExecutionException   执行服务出错时抛出
     */
    abstract protected BasicResponse doExecute(BasicRequest request)
            throws UnsupportedServiceException, ServiceExecutionException;

    @Override
    public void execute(BasicRequest t) {
        LOGGER.debug("Receive request " + t.getRequestId());

        executor.submit(() -> {

            BasicResponse response = null;

            if ((response = getCache(t)) != null) { // 获得缓存结果
                sendResponse(response);
                return;
            }

            try {
                response = doExecute(t); // 未获得缓存结果，本地执行方法
                putCache(t, response); // 执行结果放入缓存
            } catch (UnsupportedServiceException | ServiceExecutionException e) {
                LOGGER.error("Request failed: " + e.getMessage());
                response = new BasicResponse(t.getRequestId());
                response.signError();
                response.setResult(e);
            }
            sendResponse(response);
        });
    }

    /**
     * 绑定通道
     * 
     * @param channel
     */
    public void bindChannel(Channel channel) {
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
        LOGGER.debug("Fetching cached response. Request id = " + request.getRequestId());
        return cacheManager.get(request);
    }

    /**
     * 将可缓存的数据放入缓存
     * 
     * @param request
     * @param response
     */
    protected void putCache(BasicRequest request, BasicResponse response) {
        if (cacheManager == null || response == null || !response.canCacheForProvider())
            return;
        LOGGER.debug("Caching request and response. Request id = " + request.getRequestId());
        cacheManager.put(request, response);
    }

    /**
     * 发送回复
     * 
     * @param response
     */
    protected void sendResponse(BasicResponse response) {
        if (response == null)
            return;
        channel.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        LOGGER.debug("Sending response for request id=" + response.getRequestId());
    }

    @Override
    public ExecutorService getExecutorService() {
        return executor;
    }
}
