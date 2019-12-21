package conglin.clrpc.service.context;

import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Function;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class BasicProviderContext implements ProviderContext {

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


    protected Function<String, Object> objectHolder;

    @Override
    public Function<String, Object> getObjectsHolder() {
        return objectHolder;
    }

    @Override
    public void setObjectsHolder(Function<String, Object> objectHolder) {
        this.objectHolder = objectHolder;
    }
    

    protected Consumer<String> serviceRegister;

    @Override
    public Consumer<String> getServiceRegister() {
        return serviceRegister;
    }

    @Override
    public void setServiceRegister(Consumer<String> serviceRegister) {
        this.serviceRegister = serviceRegister;
    }

}