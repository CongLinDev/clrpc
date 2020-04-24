package conglin.clrpc.service.context.channel;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.context.CommonContext;

public class CommonChannelContext {

    private final Role role;

    private final PropertyConfigurer propertyConfigurer;

    private final ExecutorService executorService;

    private final SerializationHandler serializationHandler;

    public CommonChannelContext(CommonContext context) {
        role = context.role();
        propertyConfigurer = context.getPropertyConfigurer();
        executorService = context.getExecutorService();
        serializationHandler = context.getSerializationHandler();
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