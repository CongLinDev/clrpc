package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Url;
import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.zookeeper.registry.ZooKeeperServiceRegistry;

public class ProviderServiceHandler extends AbstractServiceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderServiceHandler.class);

    private ProviderContext context;

    // 服务映射
    // String保存服务名 Object保存服务实现类
    private final Map<String, Object> serviceObjects;

    private final ServiceRegistry serviceRegistry;

    public ProviderServiceHandler(PropertyConfigurer configurer) {
        super(configurer);
        serviceObjects = new HashMap<>();
        String urlString = configurer.getOrDefault("registry", "zookeeper://127.0.0.1:2181/clrpc?session-timeout=5000");
        serviceRegistry = new ZooKeeperServiceRegistry(new Url(urlString));
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
     * 发布服务元信息
     * 
     * @param serviceName
     * @param interfaceClass
     */
    public void publishServiceMetaInfo(String serviceName, Class<?> interfaceClass) {
        PropertyConfigurer c = JsonPropertyConfigurer.fromMap(ClassUtils.resolveClass(interfaceClass));
        serviceRegistry.publish(serviceName, c.toString());
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
     * 将数据注册到注册中心
     * 
     * @param address 本机地址
     */
    protected void registerService(String address) {
        PropertyConfigurer configurer = context.getPropertyConfigurer();
        serviceObjects.keySet().forEach(serviceName -> serviceRegistry.register(serviceName, address,
                configurer.subConfigurer("meta.provider." + serviceName, "meta.provider.*").toString()));
    }

    /**
     * 开启
     * 
     * @param context
     */
    public void start(ProviderContext context) {
        this.context = context;
        serviceObjects.forEach((name, bean) -> {
            PropertyConfigurer c = JsonPropertyConfigurer.fromMap(ClassUtils.resolveClass(bean.getClass()));
            serviceRegistry.publish(name, c.toString());
        });
        initContext(context);
    }

    /**
     * 初始化上下文
     * 
     * @param context
     */
    protected void initContext(ProviderContext context) {
        super.initContext(context);
        // 设置服务注册器
        context.setServiceRegister(this::registerService);
        // 设置服务对象持有器
        context.setObjectsHolder(this::getService);
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