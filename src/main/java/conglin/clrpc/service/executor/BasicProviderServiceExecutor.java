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

public class BasicProviderServiceExecutor extends AbstractProviderServiceExecutor {

    private static final Logger log = LoggerFactory.getLogger(BasicProviderServiceExecutor.class);
    
    protected final Function<String, Object> serviceObjectsHolder;

    public BasicProviderServiceExecutor(Function<String, Object> serviceObjectsHolder,
            ExecutorService executor, CacheManager<BasicRequest, BasicResponse> cacheManager){
        super(executor, cacheManager);
        this.serviceObjectsHolder = serviceObjectsHolder;
    }

    public BasicProviderServiceExecutor(Function<String, Object> serviceObjectsHolder,
            ExecutorService executor){
        super(executor);
        this.serviceObjectsHolder = serviceObjectsHolder;
    }

    @Override
    protected boolean doExecute(BasicRequest request, BasicResponse response)
            throws UnsupportedServiceException, ServiceExecutionException {
        // 该类实现下，只要不抛出异常，一定返回 true
    
        String serviceName = request.getServiceName();
        // 获取服务实现类
        Object serviceBean = serviceObjectsHolder.apply(serviceName);
        // 如果服务实现类没有注册，抛出异常
        if (serviceBean == null) {
            throw new UnsupportedServiceException(request);
        }

        jdkReflectInvoke(serviceBean, request, response);
        
        return true;
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