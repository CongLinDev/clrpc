package conglin.clrpc.service.context;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

public interface CommonContext {

    /**
     * 获取缓存管理器
     * @return
     */
    CacheManager<BasicRequest, BasicResponse> getCacheManager();

    /**
     * 设置缓存管理器
     * @param cacheManager
     */
    void setCacheManager(CacheManager<BasicRequest, BasicResponse> cacheManager);

    /**
     * 获取线程池
     * @return
     */
    ExecutorService getExecutorService();

    /**
     * 设计线程池
     * @param executorService
     */
    void setExecutorService (ExecutorService executorService);

    /**
     * 获取配置信息
     * @return
     */
    PropertyConfigurer getPropertyConfigurer();

    /**
     * 设计配置信息
     * @param propertyConfigurer
     */
    void setPropertyConfigurer(PropertyConfigurer propertyConfigurer);

    /**
     * 获取本地地址
     * @return
     */
    String getLocalAddress();

    /**
     * 设置本地地址
     */
    void setLocalAddress(String localAddress);
}