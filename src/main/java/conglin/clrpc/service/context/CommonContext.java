package conglin.clrpc.service.context;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public interface CommonContext {

    /**
     * 获取缓存管理器
     * 
     * @return
     */
    CacheManager<BasicRequest, BasicResponse> getCacheManager();

    /**
     * 设置缓存管理器
     * 
     * @param cacheManager
     */
    void setCacheManager(CacheManager<BasicRequest, BasicResponse> cacheManager);

    /**
     * 获取线程池
     * 
     * @return
     */
    ExecutorService getExecutorService();

    /**
     * 设置线程池
     * 
     * @param executorService
     */
    void setExecutorService(ExecutorService executorService);

    /**
     * 获取配置信息
     * 
     * @return
     */
    PropertyConfigurer getPropertyConfigurer();

    /**
     * 设置配置信息
     * 
     * @param propertyConfigurer
     */
    void setPropertyConfigurer(PropertyConfigurer propertyConfigurer);

    /**
     * 获取本地地址
     * 
     * @return
     */
    String getLocalAddress();

    /**
     * 设置本地地址
     */
    void setLocalAddress(String localAddress);

    /**
     * 获取序列化处理器
     * 
     * @return
     */
    SerializationHandler getSerializationHandler();

    /**
     * 设置序列化处理器
     * 
     * @param serializationHandler
     */
    void setSerializationHandler(SerializationHandler serializationHandler);

    /**
     * 获取本机的元信息
     * 
     * @return
     */
    String getMetaInformation();

    /**
     * 设置本机的元信息
     * 
     * @param metaInfo
     */
    void setMetaInformation(String metaInfo);
}