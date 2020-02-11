package conglin.clrpc.service.context;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.serialization.SerializationHandler;

public class BasicCommonContext implements CommonContext {

    private ExecutorService executorService;

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    private PropertyConfigurer propertyConfigurer;

    @Override
    public PropertyConfigurer getPropertyConfigurer() {
        return propertyConfigurer;
    }

    @Override
    public void setPropertyConfigurer(PropertyConfigurer propertyConfigurer) {
        this.propertyConfigurer = propertyConfigurer;
    }

    private InetSocketAddress localAddress;

    @Override
    public InetSocketAddress getLocalAddress() {
        return localAddress;
    }

    @Override
    public void setLocalAddress(InetSocketAddress localAddress) {
        this.localAddress = localAddress;
    }

    private SerializationHandler serializationHandler;

    @Override
    public SerializationHandler getSerializationHandler() {
        return serializationHandler;
    }

    @Override
    public void setSerializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
    }

    private Map<String, Object> extensionObjects = new HashMap<>();

    @Override
    public Map<String, Object> getExtensionObject() {
        return extensionObjects;
    }

}