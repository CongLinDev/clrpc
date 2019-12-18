package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.registry.ServiceRegistry;
import conglin.clrpc.registry.ZooKeeperServiceRegistry;
import conglin.clrpc.service.context.ProviderContext;

public class ProviderServiceHandler extends AbstractServiceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderServiceHandler.class);

    // 服务映射
    // String保存服务名 Object保存服务实现类
    private final Map<String, Object> serviceObjects;

    private final String LOCAL_ADDRESS;

    private final ServiceRegistry serviceRegistry;

    public ProviderServiceHandler(PropertyConfigurer configurer) {
        super(configurer);
        this.LOCAL_ADDRESS = configurer.getOrDefault("provider.address", "127.0.0.1:5100");
        serviceObjects = new HashMap<>();
        serviceRegistry = new ZooKeeperServiceRegistry(configurer);
    }

    /**
     * 手动添加服务 此时服务并未注册 且若服务名相同，后添加的服务会覆盖前添加的服务
     * 
     * @param serviceName
     * @param serviceBean
     */
    public void publish(String serviceName, Object serviceBean) {
        serviceObjects.put(serviceName, serviceBean);
    }

    /**
     * 查找 提供服务的Bean
     * 
     * @param serviceName
     * @return
     */
    public Object getService(String serviceName) {
        return serviceObjects.get(serviceName);
    }

    /**
     * 移除服务
     * 
     * @param serviceName 服务名
     */
    public void removeService(String serviceName) {
        serviceObjects.remove(serviceName);
        LOGGER.debug("Remove service named " + serviceName);
    }

    /**
     * 注册到zookeeper
     * 
     * @param data
     */
    protected void registerService(String data) {
        // 把服务名注册到zookeeper上
        serviceObjects.keySet().forEach(serviceName -> serviceRegistry.register(serviceName, data));
    }

    /**
     * 将本机地址注册到zookeeper上
     */
    protected void registerService() {
        serviceObjects.keySet().forEach(serviceName -> serviceRegistry.register(serviceName, LOCAL_ADDRESS));
    }

    /**
     * 开启
     * 
     * @param context
     */
    public void start(ProviderContext context) {
        // 设置服务注册器
        context.setServiceRegister(this::registerService);
        // 设置服务对象持有器
        context.setObjectsHolder(this::getService);
        // 设置线程池
        context.setExecutorService(getExecutorService());
    }

    /**
     * 关闭
     */
    public void stop() {
        if (!super.isDestroyed()) {
            try {
                super.destroy();
            } catch (DestroyFailedException e) {
                LOGGER.error(e.getMessage());
            }
        }
    }
}