package conglin.clrpc.service.context;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.definition.role.Role;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.instance.ServiceInstanceCodec;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.protocol.ProtocolDefinition;
import conglin.clrpc.transport.router.Router;

import java.util.Map;
import java.util.Properties;

public enum RpcContextEnum {
    ROLE(Role.class),
    SERIALIZATION_HANDLER(SerializationHandler.class),
    IDENTIFIER_GENERATOR(IdentifierGenerator.class),
    ROUTER(Router.class),
    PROPERTIES(Properties.class),
    REQUEST_SENDER(RequestSender.class),
    FUTURE_HOLDER(FutureHolder.class),
    SERVICE_OBJECT_HOLDER(Map.class),
    SERVICE_INSTANCE_CODEC(ServiceInstanceCodec.class),
    PROTOCOL_DEFINITION(ProtocolDefinition.class);


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
