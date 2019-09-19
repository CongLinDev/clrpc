package conglin.clrpc.transfer.receiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.annotation.CacheableService;
import conglin.clrpc.common.annotation.IgnoreService;
import conglin.clrpc.common.exception.NoSuchServiceException;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

public class BasicRequestReceiver implements RequestReceiver {

    private static final Logger log = LoggerFactory.getLogger(BasicRequestReceiver.class);

    protected ProviderServiceHandler serviceHandler;

    protected CacheManager<BasicRequest, BasicResponse> cacheManager;

    public BasicRequestReceiver(){
        
    }

    @Override
    public void init(ProviderServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }


    @Override
    public void bindCachePool(CacheManager<BasicRequest, BasicResponse> cacheManager) {
        this.cacheManager = cacheManager;
    }

    @Override
    public BasicResponse handleRequest(BasicRequest request) {
        log.debug("Receive request " + request.getRequestId());
        BasicResponse response = null;

        // fetch from cache
        if((response = getCache(request)) != null) return response;

        response = new BasicResponse();
        response.setRequestId(request.getRequestId());
        try {
            doHandleRequest(request, response);

            // put cache
            putCache(request, response);
        } catch (NoSuchServiceException | ServiceExecutionException e) {
            log.error("Request failed: " + e.getMessage());
            response.signError();
            response.setResult(e);
        }
        return response;
    }

    @Override
    public void stop() {
        // do nothing
    }

    @Override
    public ExecutorService getExecutorService() {
        return serviceHandler.getExecutorService();
    }
    
    /**
     * 检查缓存中是否有需要的结果
     * @param request
     * @return
     */
    protected BasicResponse getCache(BasicRequest request){
        if(cacheManager == null) return null;
        log.debug("Fetching cached response. Request id = " + request.getRequestId());
        return cacheManager.get(request);
    }

    /**
     * 将可缓存的数据放入缓存
     * @param request
     * @param response
     */
    protected void putCache(BasicRequest request, BasicResponse response){
        if(cacheManager == null || !response.canCacheForProvider()) return;
        log.debug("Caching request and response. Request id = " + request.getRequestId());
        cacheManager.put(request, response);
    }

    /**
     * 处理客户端请求并生成结果
     * @param request
     * @param response
     * @throws InvocationTargetException
     */
    protected void doHandleRequest(BasicRequest request, BasicResponse response) throws NoSuchServiceException, ServiceExecutionException{

        String serviceName = request.getServiceName();
        //获取服务实现类
        Object serviceBean = serviceHandler.getService(serviceName);
        //如果服务实现类没有注册，抛出异常
        if(serviceBean == null){
            throw new NoSuchServiceException(request);
        }
        
        jdkReflectInvoke(serviceBean, request, response);
    }

    /**
     * 使用jdk反射来调用方法
     * @param serviceBean
     * @param request
     * @param response
     * @throws ServiceExecutionException
     */
    protected void jdkReflectInvoke(Object serviceBean, BasicRequest request, BasicResponse response)
            throws ServiceExecutionException{
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
     * @param method
     * @param response
     * @throws NoSuchMethodException
     */
    
    protected void handleAnnotation(Method method, BasicResponse response) throws NoSuchMethodException{
        // 处理 {@link IgnoreService} 
        IgnoreService ignoreService = method.getAnnotation(IgnoreService.class);
        if(ignoreService != null && ignoreService.ignore())
            throw new NoSuchMethodException(method.getName());
        
        // 处理{@link CacheableService}
        CacheableService cacheableService = method.getAnnotation(CacheableService.class);
        if(cacheableService == null) return;// 默认值为0，不必调用方法设置为0
        if(cacheableService.consumer()){
            response.canCacheForConsumer();
            response.setExpireTime(cacheableService.exprie());
        }
        if(cacheableService.provider()){
            response.canCacheForProvider();
            response.setExpireTime(cacheableService.exprie());
        }
    }



    // remove cglib reflect
    // private Object cglibReflectInvoke(Object serviceBean, BasicRequest request) throws ServiceExecutionException {
    //     Class<?> serviceBeanClass = serviceBean.getClass();
    //     String methodName = request.getMethodName();
    //     Class<?>[] parameterTypes = request.getParameterTypes();
    //     Object[] parameters = request.getParameters();

    //     log.debug("Invoking class..." + serviceBeanClass.getName());
    //     log.debug("Invoking method..." + methodName);

    //     //使用CGLib反射动态加载
    //     FastClass serviceFastClass = FastClass.create(serviceBeanClass);
    //     int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);
    //     try {
    //         return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    //     } catch (InvocationTargetException e) {
    //         throw new ServiceExecutionException(request, e);
    //     }
    // }
}