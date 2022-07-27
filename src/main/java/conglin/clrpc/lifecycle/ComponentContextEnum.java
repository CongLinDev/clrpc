package conglin.clrpc.lifecycle;

import java.util.Properties;

import conglin.clrpc.common.Role;
import conglin.clrpc.executor.pipeline.ExecutorPipeline;
import conglin.clrpc.invocation.identifier.IdentifierGenerator;
import conglin.clrpc.invocation.protocol.ProtocolDefinition;
import conglin.clrpc.invocation.serialization.SerializationHandler;
import conglin.clrpc.service.ServiceObjectHolder;
import conglin.clrpc.service.instance.codec.ServiceInstanceCodec;
import conglin.clrpc.service.router.Router;

public enum ComponentContextEnum {
    ROLE(Role.class),
    SERIALIZATION_HANDLER(SerializationHandler.class),
    IDENTIFIER_GENERATOR(IdentifierGenerator.class),
    ROUTER(Router.class),
    PROPERTIES(Properties.class),
    EXECUTOR_PIPELINE(ExecutorPipeline.class),
    SERVICE_OBJECT_HOLDER(ServiceObjectHolder.class),
    SERVICE_INSTANCE_CODEC(ServiceInstanceCodec.class),
    PROTOCOL_DEFINITION(ProtocolDefinition.class);


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
