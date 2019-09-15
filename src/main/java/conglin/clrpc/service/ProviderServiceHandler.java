package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.registry.BasicServiceRegistry;
import conglin.clrpc.service.registry.ServiceRegistry;

public class ProviderServiceHandler extends AbstractServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ProviderServiceHandler.class);

    //服务映射
    //String保存服务名 Object保存服务实现类
    private final Map<String, Object> services;

    private final String LOCAL_ADDRESS;

    private final ServiceRegistry serviceRegistry;

    public ProviderServiceHandler(String localAddress) {
        super();
        this.LOCAL_ADDRESS = localAddress;
        services = new HashMap<>();
        serviceRegistry = new BasicServiceRegistry();
    }

    /**
     * 手动添加服务 此时服务并未注册
     * 且若服务名相同，后添加的服务会覆盖前添加的服务
     * @param serviceName
     * @param serviceBean
     */
    public void publishService(String serviceName, Object serviceBean){
        services.put(serviceName, serviceBean);
    }

    /**
     * 查找 提供服务的Bean
     * @param serviceName
     * @return
     */
    public Object getService(String serviceName){
        return services.get(serviceName);
    }

    /**
     * 移除服务
     * @param serviceName 服务名
     */
    public void removeService(String serviceName){
        services.remove(serviceName);
        log.debug("Remove service named " + serviceName);
    }

    /**
     * 注册到zookeeper
     * @param data
     */
    public void registerService(String data){
        //把服务名注册到zookeeper上
        services.keySet().forEach(
            serviceName -> serviceRegistry.registerProvider(serviceName, data)
        );
    }

    /**
     * 将本机地址注册到zookeeper上
     */
    public void registerService(){
        services.keySet().forEach(
            serviceName -> serviceRegistry.registerProvider(serviceName, LOCAL_ADDRESS)
        ); 
    }

    public void start(){
        // do nothing
    }
    
    @Override
    public void stop() {
        super.stop();
        serviceRegistry.stop();
    }
}