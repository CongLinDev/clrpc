package conglin.clrpc.service.context;

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

    private String localAddress;

    @Override
    public String getLocalAddress() {
        return localAddress;
    }

    @Override
    public void setLocalAddress(String localAddress) {
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

    private String metaInfo;

    @Override
    public String getMetaInformation() {
        return metaInfo;
    }

    @Override
    public void setMetaInformation(String metaInfo) {
        this.metaInfo = metaInfo;
    }

    private Map<String, Object> extensionObjects = new HashMap<>();

    @Override
    public Map<String, Object> getExtensionObject() {
        return extensionObjects;
    }

}