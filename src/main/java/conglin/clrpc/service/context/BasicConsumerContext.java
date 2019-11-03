package conglin.clrpc.service.context;

import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.executor.AbstractConsumerServiceExecutor;
import conglin.clrpc.service.executor.RequestSender;
import conglin.clrpc.service.executor.ServiceExecutor;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.Channel;

public class BasicConsumerContext implements ConsumerContext {
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


    protected AbstractConsumerServiceExecutor consumerServiceExecutor;

    @Override
    public void setConsumerServiceExecutor(AbstractConsumerServiceExecutor consumerServiceExecutor) {
        this.consumerServiceExecutor = consumerServiceExecutor;
    }

    @Override
    public ServiceExecutor<BasicResponse> getServiceExecutor() {
        return consumerServiceExecutor;
    }

    @Override
    public RequestSender getRequestSender() {
        return consumerServiceExecutor;
    }



    protected FuturesHolder<Long> futuresHolder;

    @Override
    public FuturesHolder<Long> getFuturesHolder() {
        return futuresHolder;
    }

    @Override
    public void setFuturesHolder(FuturesHolder<Long> futuresHolder) {
        this.futuresHolder = futuresHolder;
    }


    BiFunction<String, Object, Channel> providerChooser;

    @Override
    public BiFunction<String, Object, Channel> getProviderChooser() {
        return providerChooser;
    }

    @Override
    public void setProviderChooser(BiFunction<String, Object, Channel> providerChooser) {
        this.providerChooser = providerChooser;
    }

}