package conglin.clrpc.service.context;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.registry.ServiceDiscovery;
import conglin.clrpc.common.registry.ServiceRegistry;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.router.instance.ServiceInstanceGenerator;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.transport.component.ProviderChooser;
import conglin.clrpc.transport.component.ProviderChooserAdapter;
import conglin.clrpc.transport.component.RequestSender;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public enum RpcContextEnum {
    ROLE(Role.class),
    SERIALIZATION_HANDLER(SerializationHandler.class),
    IDENTIFIER_GENERATOR(IdentifierGenerator.class),
    PROVIDER_CHOOSER_ADAPTER(ProviderChooserAdapter.class),
    PROVIDER_CHOOSER(ProviderChooser.class),
    PROPERTY_CONFIGURER(PropertyConfigurer.class),
    EXECUTOR_SERVICE(ExecutorService.class),
    REQUEST_SENDER(RequestSender.class),

    SERVICE_DISCOVERY(ServiceDiscovery.class),
    SERVICE_REGISTRY(ServiceRegistry.class),
    FUTURE_HOLDER(FutureHolder.class),

    SERVICE_OBJECT_HOLDER(Map.class),
    SERVICE_INSTANCE_GENERATOR(ServiceInstanceGenerator.class);


    private final Class<?> clazz;

    RpcContextEnum(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * 是否接受对象
     *
     * @param obj
     * @return
     */
    public boolean accept(Object obj) {
        return clazz.isAssignableFrom(obj.getClass());
    }

    /**
     * 接收的类
     *
     * @return
     */
    public Class<?> acceptClass() {
        return clazz;
    }
}
