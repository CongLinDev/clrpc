package conglin.clrpc.transfer.net.receiver;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchServiceException;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

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
    public void handleRequest(Channel channel, BasicRequest request) {
        serviceHandler.submit(() -> {
            log.debug("Receive request " + request.getRequestId());
            BasicResponse response = BasicResponse.builder()
                    .requestId(request.getRequestId())
                    .build();
            try {
                Object result = handleRequestCore(request);
                response.setResult(result);
            } catch (NoSuchServiceException | ServiceExecutionException e) {
                log.error("Request failed: " + e.getMessage());
                response.setError(e.getDescription());
            }

            channel.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
            log.debug("Sending response for request id=" + request.getRequestId());
        });
    }

    @Override
    public void stop() {
        // do nothing
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
            method.setAccessible(true);
            return method.invoke(serviceBean, parameters);
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            throw new ServiceExecutionException(request, e);
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