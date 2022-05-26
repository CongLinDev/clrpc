package conglin.clrpc.service.context;

import java.util.Properties;

import conglin.clrpc.common.Role;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.ServiceObjectHolder;
import conglin.clrpc.service.handler.factory.ChannelHandlerFactory;
import conglin.clrpc.service.instance.codec.ServiceInstanceCodec;
import conglin.clrpc.service.registry.ServiceRegistry;
import conglin.clrpc.transport.component.InvocationExecutor;
import conglin.clrpc.transport.protocol.ProtocolDefinition;
import conglin.clrpc.transport.router.Router;

public enum ComponentContextEnum {
    ROLE(Role.class),
    SERIALIZATION_HANDLER(SerializationHandler.class),
    IDENTIFIER_GENERATOR(IdentifierGenerator.class),
    ROUTER(Router.class),
    PROPERTIES(Properties.class),
    INVOCATION_EXECUTOR(InvocationExecutor.class),
    INVOCATION_CONTEXT_HOLDER(InvocationContextHolder.class),
    SERVICE_OBJECT_HOLDER(ServiceObjectHolder.class),
    SERVICE_INSTANCE_CODEC(ServiceInstanceCodec.class),
    PROTOCOL_DEFINITION(ProtocolDefinition.class),
    SERVICE_REGISTRY(ServiceRegistry.class),
    CHANNEL_HANDLER_FACTORY(ChannelHandlerFactory.class);


    private final Class<?> clazz;

    ComponentContextEnum(Class<?> clazz) {
        this.clazz = clazz;
    }

    /**
     * 是否接受对象
     *
     * @param obj
     * @return
     */
    public boolean accept(Object obj) {
        return obj == null || clazz.isAssignableFrom(obj.getClass());
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
