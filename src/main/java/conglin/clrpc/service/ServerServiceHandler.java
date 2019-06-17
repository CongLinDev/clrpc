package conglin.clrpc.service;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.registry.BasicServiceRegistry;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.transfer.net.BasicRequest;

import net.sf.cglib.reflect.FastClass;

public class ServerServiceHandler extends ServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ServerServiceHandler.class);

    //服务映射
    //String保存接口名 Object保存服务实现类
    private Map<String, Object> services;

    private ServiceRegistry serviceRegistry;

    public ServerServiceHandler() {
        services = new HashMap<>();
        serviceRegistry = new BasicServiceRegistry();
    }

    /**
     * 寻找所有使用 {@link conglin.clrpc.service.RpcService} 注解的接口或类，将其加入到
     * {@link RpcServiceHandler#services} 中
     */
    public void findService() {
        // TODO: 使用Spring获取带有注解的类或接口
    }

    /**
     * 手动添加服务 此时服务并未注册
     * 且若服务接口相同，后添加的服务会覆盖前添加的服务
     * @param interfaceClass
     * @param implementClass
     */
    public void addService(Class<?> interfaceClass, Class<?> implementClass) {
        if (implementClass.isInterface()) {
            log.error("{} is not a service class. And it will not be added Services", implementClass.getName());
        } else if (!interfaceClass.isAssignableFrom(implementClass)) {
            log.error("{} is not permitted. And it will not be added Services", implementClass.getName());
        } else {
            try {
                Object implementObject = implementClass.getDeclaredConstructor().newInstance();
                services.put(interfaceClass.getName(), implementObject);
            } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                    | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                log.error("{} Can not add service", e);
            }
        }
    }

    /**
     * 注册到zookeeper
     * @param data
     */
    public void registerService(String data){
        serviceRegistry.register(data);
    }


    /**
     * 处理客户端请求并生成结果
     * @param request
     * @return
     * @throws InvocationTargetException
     */
    public Object handleRequest(BasicRequest request) throws InvocationTargetException{
        String className = request.getClassName();

        //获取服务实现类
        Object serviceBean = services.get(className);
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

}