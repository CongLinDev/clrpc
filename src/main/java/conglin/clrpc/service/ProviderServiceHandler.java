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

    private ProviderContext context;

    // 服务映射
    // String保存服务名 Object保存服务实现类
    private final Map<String, Object> serviceObjects;

    private ServiceRegistry serviceRegistry;

    public ProviderServiceHandler(PropertyConfigurer configurer) {
        super(configurer);
        serviceObjects = new HashMap<>();
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
    public void unregisterService(String serviceName) {
        serviceRegistry.unregister(serviceName);
        serviceObjects.remove(serviceName);
        LOGGER.debug("Remove service named {}", serviceName);
    }

    /**
     * 将数据注册到注册中心
     */
    protected void registerService() {
        PropertyConfigurer configurer = context.getPropertyConfigurer();
        serviceObjects.keySet().forEach(serviceName -> serviceRegistry.register(serviceName,
                configurer.subConfigurer("meta.provider." + serviceName, "meta.provider.*").toString()));
    }

    /**
     * 开启
     * 
     * @param context
     */
    public void start(ProviderContext context) {
        serviceRegistry = new ZooKeeperServiceRegistry(context.getLocalAddress(), context.getPropertyConfigurer());
        this.context = context;
        initContext(context);
    }

    /**
     * 初始化上下文
     * 
     * @param context
     */
    protected void initContext(ProviderContext context) {
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