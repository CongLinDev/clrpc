package conglin.clrpc.service.context;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class BasicCommonContext implements CommonContext {

    protected CacheManager<BasicRequest, BasicResponse> cacheManager;

    @Override
    public CacheManager<BasicRequest, BasicResponse> getCacheManager() {
        return cacheManager;
    }

    @Override
    public void setCacheManager(CacheManager<BasicRequest, BasicResponse> cacheManager) {
        this.cacheManager = cacheManager;
    }

    protected ExecutorService executorService;

    @Override
    public ExecutorService getExecutorService() {
        return executorService;
    }

    @Override
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    protected PropertyConfigurer propertyConfigurer;
    
    @Override
    public PropertyConfigurer getPropertyConfigurer() {
        return propertyConfigurer;
    }

    @Override
    public void setPropertyConfigurer(PropertyConfigurer propertyConfigurer) {
        this.propertyConfigurer = propertyConfigurer;
    }

    protected String localAddress;

    @Override
    public String getLocalAddress() {
        return localAddress;
    }

    @Override
    public void setLocalAddress(String localAddress) {
        this.localAddress = localAddress;
    }

    protected SerializationHandler serializationHandler;

    @Override
    public SerializationHandler getSerializationHandler() {
        return serializationHandler;
    }

    @Override
    public void setSerializationHandler(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;        
    }

    protected String metaInfo;

    @Override
    public String getMetaInformation() {
        return metaInfo;
    }

    @Override
    public void setMetaInformation(String metaInfo) {
        this.metaInfo = metaInfo;
    }

}