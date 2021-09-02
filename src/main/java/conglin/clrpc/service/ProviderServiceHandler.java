package conglin.clrpc.service;

import java.util.HashMap;
import java.util.Map;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.common.registry.ServiceRegistry;

public class ProviderServiceHandler extends AbstractServiceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderServiceHandler.class);

    // 服务映射
    // String保存服务名 Object保存服务实现类
    private final Map<String, ServiceObject> serviceObjects;

    private final ServiceRegistry serviceRegistry;

    public ProviderServiceHandler(PropertyConfigurer configurer) {
        super(configurer);
        serviceObjects = new HashMap<>();
        String registryClassName = configurer.get("registry.register-class", String.class);
        String registryUrl = configurer.get("registry.url", String.class);
        serviceRegistry = ClassUtils.loadObjectByType(registryClassName, ServiceRegistry.class, new UrlScheme(registryUrl));
    }

    /**
     * 手动添加服务 此时服务并未注册 且若服务名相同，后添加的服务会覆盖前添加的服务
     *
     * @param serviceObject
     */
    public void publish(ServiceObject serviceObject) {
        serviceObjects.put(serviceObject.name(), serviceObject);
        LOGGER.info("Publish service named {} with bean(class={}).", serviceObject.name(), serviceObject.objectClass());
    }

    @Override
    public void start(RpcContext context) {
        super.start(context);
        // 设置服务对象持有器
        context.put(RpcContextEnum.SERVICE_OBJECT_HOLDER, this.serviceObjects);
        context.put(RpcContextEnum.SERVICE_REGISTRY, this.serviceRegistry);
    }

    @Override
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