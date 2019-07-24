package conglin.clrpc.service;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchServiceException;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.service.registry.BasicServiceRegistry;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.transfer.net.message.BasicRequest;

public class ServerServiceHandler extends AbstractServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ServerServiceHandler.class);

    //服务映射
    //String保存接口名 Object保存服务实现类
    private final Map<String, Object> services;

    private final ServiceRegistry serviceRegistry;

    public ServerServiceHandler() {
        super();
        services = new HashMap<>();
        serviceRegistry = new BasicServiceRegistry();
    }

    /**
     * 手动添加服务 此时服务并未注册
     * 且若服务接口相同，后添加的服务会覆盖前添加的服务
     * @param serviceName
     * @param implementClass
     */
    public void addService(String serviceName, Class<?> implementClass){
        try {
            Object implementObject = implementClass.getDeclaredConstructor().newInstance();
            services.put(serviceName, implementObject);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            log.error("Can not add service. " + e.getMessage());
        }
    }

    /**
     * 注册到zookeeper
     * @param data
     */
    public void registerService(String data){
        //只把简单的类名注册到zookeeper上
        services.keySet().forEach(
            serviceName -> serviceRegistry.registerProvider(serviceName, data)
        );
    }

    /**
     * 处理客户端请求并生成结果
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    public Object handleRequest(BasicRequest request) throws NoSuchServiceException, ServiceExecutionException{

        String serviceName = request.getServiceName();
        //获取服务实现类
        Object serviceBean = services.get(serviceName);
        //如果服务实现类没有注册，抛出异常
        if(serviceBean == null){
            throw new NoSuchServiceException(request);
        }
        
        return jdkReflectInvoke(serviceBean, request);
    }

    private Object jdkReflectInvoke(Object serviceBean, BasicRequest request)
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

    /**
     * 移除服务
     * @param serviceName 服务名
     */
    public void removeService(String serviceName){
        services.remove(serviceName);
        log.debug("Remove service named " + serviceName);
    }
}