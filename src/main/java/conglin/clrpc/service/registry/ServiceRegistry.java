package conglin.clrpc.service.registry;

import java.util.Collection;
import java.util.List;
import java.util.function.Consumer;

import conglin.clrpc.service.ServiceInterface;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.instance.ServiceInstance;

public interface ServiceRegistry {
    /**
     * 注册 provider
     * 
     * 由 provider 端调用
     * 
     * @param serviceObject
     */
    void registerProvider(ServiceObject<?> serviceObject);

    /**
     * 取消注册 provider
     * 
     * 由 provider 端调用
     * 
     * @param serviceObject
     */
    void unregisterProvider(ServiceObject<?> serviceObject);

    /**
     * 订阅 provider
     * 
     * 由 consumer 端调用
     * 
     * @param serviceInterface
     * @param callback
     */
    void subscribeProvider(ServiceInterface<?> serviceInterface, Consumer<Collection<ServiceInstance>> callback);

    /**
     * 注册 consumer
     * 
     * 由 consumer 端调用
     * 
     * @param serviceInterface
     */
    void registerConsumer(ServiceInterface<?> serviceInterface);

    /**
     * 取消注册 consumer
     * 
     * 由 consumer 端调用
     * 
     * @param serviceInterface
     */
    void unregisterConsumer(ServiceInterface<?> serviceInterface);

    /**
     * 列出可用的 provider
     * 
     * 由 consumer 端调用
     * 
     * @param serviceInterface
     */
    List<ServiceInstance> listProviders(ServiceInterface<?> serviceInterface);
}
