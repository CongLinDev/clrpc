package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.cache.caffeine.CaffeineCacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

abstract public class Bootstrap{
    protected final boolean ENABLE_CACHE;
    protected final CacheManager<BasicRequest, BasicResponse> cacheManager;

    protected final PropertyConfigurer configurer;

    public Bootstrap(PropertyConfigurer configurer){
        this(configurer, configurer.getOrDefault("service.cache.enable", false));
    }

    public Bootstrap(PropertyConfigurer configurer, boolean enableCache) {
        this.configurer = configurer;
        this.ENABLE_CACHE = enableCache;
        if(ENABLE_CACHE){
            cacheManager = new CaffeineCacheManager(configurer);
        }else{
            cacheManager = null;
        }
    }
}