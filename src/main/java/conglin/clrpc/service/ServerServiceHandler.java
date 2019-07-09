package conglin.clrpc.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchServiceException;
import conglin.clrpc.service.registry.BasicServiceRegistry;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.transfer.net.message.BasicRequest;

import net.sf.cglib.reflect.FastClass;

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
     * @param interfaceClass
     * @param implementClass
     */
    public void addService(Class<?> interfaceClass, Class<?> implementClass) {
        if (implementClass.isInterface()) {
            log.error(implementClass.getName() + " is not a service class. And it will not be added Services");
        } else if (!interfaceClass.isAssignableFrom(implementClass)) {
            log.error(implementClass.getName() + " is not permitted. And it will not be added Services");
        } else {
            addService(interfaceClass.getName(), implementClass);
        }
    }

    /**
     * 手动添加服务 此时服务并未注册
     * 且若服务接口相同，后添加的服务会覆盖前添加的服务
     * @param implementClass 该实现类类名必须满足 'xxxServiceImpl' 格式
     */
    public void addService(Class<?> implementClass){
        String implementClassName = implementClass.getName();
        if (implementClass.isInterface()) {
            log.error(implementClassName + " is not a service class. And it will not be added Services");
        }else if (!implementClassName.endsWith("ServiceImpl")){
            log.error(implementClassName + " is not permitted. And you must use 'xxxServiceImpl' format classname.");
        }else{
            addService(implementClassName.substring(0, implementClassName.length()-4), implementClass);
        }
    }

    /**
     * 手动添加服务 此时服务并未注册
     * 且若服务接口相同，后添加的服务会覆盖前添加的服务
     * @param interfaceClassName
     * @param implementClass
     */
    public void addService(String interfaceClassName, Class<?> implementClass){
        try {
            Object implementObject = implementClass.getDeclaredConstructor().newInstance();
            services.put(interfaceClassName, implementObject);
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
            service -> serviceRegistry.registerProvider(
                service.substring(service.lastIndexOf(".") + 1, service.length()),
                data)
            );
    }

    /**
     * 处理客户端请求并生成结果
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    public Object handleRequest(BasicRequest request) throws InvocationTargetException, NoSuchServiceException{
        String className = request.getClassName();

        //获取服务实现类
        Object serviceBean = services.get(className);
        //如果服务实现类没有注册，抛出异常
        if(serviceBean == null){
            throw new NoSuchServiceException(request.getRequestId(), className, request.getMethodName());
        }

        Class<?> serviceBeanClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        log.debug("Invoking class..." + serviceBeanClass.getName());
        log.debug("Invoking method..." + methodName);

        //使用CGLib反射动态加载
        FastClass serviceFastClass = FastClass.create(serviceBeanClass);
        int methodIndex = serviceFastClass.getIndex(methodName, parameterTypes);
        return serviceFastClass.invoke(methodIndex, serviceBean, parameters);
    }

    /**
     * 移除服务
     * @param interfaceClass 接口
     */
    public void removeService(Class<?> interfaceClass){
        services.remove(interfaceClass.getName());
        log.debug("Remove service named " + interfaceClass.getName());
    }
}