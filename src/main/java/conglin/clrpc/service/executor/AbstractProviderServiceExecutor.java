package conglin.clrpc.service.executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.annotation.CacheableService;
import conglin.clrpc.common.annotation.IgnoreService;
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

    protected final Function<String, Object> serviceObjectsHolder;

    private final CacheManager<BasicRequest, BasicResponse> cacheManager;

    private Channel channel;

    public AbstractProviderServiceExecutor(Function<String, Object> serviceObjectsHolder, ExecutorService executor) {
        this(serviceObjectsHolder, executor, null);
    }

    public AbstractProviderServiceExecutor(Function<String, Object> serviceObjectsHolder, 
                    ExecutorService executor,
                    CacheManager<BasicRequest, BasicResponse> cacheManager){
        this.serviceObjectsHolder = serviceObjectsHolder;
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
    abstract boolean doExecute(BasicRequest request, BasicResponse response)
        throws UnsupportedServiceException, ServiceExecutionException;

    @Override
    public ExecutorService getExecutorService() {
        return executor;
    }

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
                    putCache(t, response);

                }catch(UnsupportedServiceException | ServiceExecutionException e){
                    log.error("Request failed: " + e.getMessage());
                    response.signError();
                    response.setResult(e);
                }
            } else {
                executeCompletely = true;
            }
            if(executeCompletely)
                send(response);
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
    protected void send(BasicResponse response) {
        if(response == null) return;
        channel.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Sending response for request id=" + response.getRequestId());
    }

    /**
     * 使用jdk反射来调用方法
     * 
     * @param serviceBean
     * @param request
     * @param response
     * @throws ServiceExecutionException
     */
    protected void jdkReflectInvoke(Object serviceBean, BasicRequest request, BasicResponse response)
            throws ServiceExecutionException {
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        log.debug("Invoking class..." + serviceBeanClass.getName());
        log.debug("Invoking method..." + methodName);

        try {
            Method method = serviceBeanClass.getMethod(methodName, parameterTypes);
            handleAnnotation(method, response);
            method.setAccessible(true);
            response.setResult(method.invoke(serviceBean, parameters));
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(request, e);
        }
    }

    /**
     * 处理注解
     * 
     * @param method
     * @param response
     * @throws NoSuchMethodException
     */
    protected void handleAnnotation(Method method, BasicResponse response) throws NoSuchMethodException {
        // 处理 {@link IgnoreService}
        IgnoreService ignoreService = method.getAnnotation(IgnoreService.class);
        if (ignoreService != null && ignoreService.ignore())
            throw new NoSuchMethodException(method.getName());

        // 处理{@link CacheableService}
        CacheableService cacheableService = method.getAnnotation(CacheableService.class);
        if (cacheableService == null)
            return;// 默认值为0，不必调用方法设置为0
        if (cacheableService.consumer()) {
            response.canCacheForConsumer();
            response.setExpireTime(cacheableService.exprie());
        }
        if (cacheableService.provider()) {
            response.canCacheForProvider();
            response.setExpireTime(cacheableService.exprie());
        }
    }
}

