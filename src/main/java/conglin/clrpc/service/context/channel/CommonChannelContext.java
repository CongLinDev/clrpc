package conglin.clrpc.service.context.channel;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;

public class CommonChannelContext {

    private final Role role;

    private final PropertyConfigurer propertyConfigurer;

    private final ExecutorService executorService;

    private final SerializationHandler serializationHandler;

    public CommonChannelContext(RpcContext context) {
        role = context.getWith(RpcContextEnum.ROLE);
        propertyConfigurer = context.getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        executorService = context.getWith(RpcContextEnum.EXECUTOR_SERVICE);
        serializationHandler = context.getWith(RpcContextEnum.SERIALIZATION_HANDLER);
    }

    /**
     * @return the role
     */
    public Role role() {
        return role;
    }

    /**
     * @return the propertyConfigurer
     */
    public PropertyConfigurer propertyConfigurer() {
        return propertyConfigurer;
    }

    /**
     * @return the executorService
     */
    public ExecutorService executorService() {
        return executorService;
    }

    /**
     * @return the serializationHandler
     */
    public SerializationHandler serializationHandler() {
        return serializationHandler;
    }
}