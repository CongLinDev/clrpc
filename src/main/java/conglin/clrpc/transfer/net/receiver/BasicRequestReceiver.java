package conglin.clrpc.transfer.net.receiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.annotation.IgnoreService;
import conglin.clrpc.common.exception.NoSuchServiceException;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;

public class BasicRequestReceiver implements RequestReceiver {

    private static final Logger log = LoggerFactory.getLogger(BasicRequestReceiver.class);

    protected ServerServiceHandler serviceHandler;

    public BasicRequestReceiver(){
        
    }

    @Override
    public void init(ServerServiceHandler serviceHandler) {
        this.serviceHandler = serviceHandler;
    }

    @Override
    public BasicResponse handleRequest(BasicRequest request) {
        log.debug("Receive request " + request.getRequestId());
        BasicResponse response = new BasicResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handleRequestCore(request);
            response.setResult(result);
        } catch (NoSuchServiceException | ServiceExecutionException e) {
            log.error("Request failed: " + e.getMessage());
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
        //if(serviceHandler == null) return null;
        return serviceHandler.getExecutorService();
	}

    /**
     * 处理客户端请求并生成结果
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    protected Object handleRequestCore(BasicRequest request) throws NoSuchServiceException, ServiceExecutionException{

        String serviceName = request.getServiceName();
        //获取服务实现类
        Object serviceBean = serviceHandler.getService(serviceName);
        //如果服务实现类没有注册，抛出异常
        if(serviceBean == null){
            throw new NoSuchServiceException(request);
        }
        
        return jdkReflectInvoke(serviceBean, request);
    }

    /**
     * 使用jdk反射来调用方法
     * @param serviceBean
     * @param request
     * @return
     * @throws ServiceExecutionException
     */
    protected Object jdkReflectInvoke(Object serviceBean, BasicRequest request)
            throws ServiceExecutionException{
        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        log.debug("Invoking class..." + serviceBeanClass.getName());
        log.debug("Invoking method..." + methodName);
   
        try {
            Method method = serviceBeanClass.getMethod(methodName, parameterTypes);
            handleAnnotation(method);
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(request, e);
        }
    }

    protected void handleAnnotation(Method method) throws NoSuchMethodException{
        IgnoreService ignoreService = method.getAnnotation(IgnoreService.class);
        if(ignoreService != null && ignoreService.ignore())
            throw new NoSuchMethodException(method.getName());
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