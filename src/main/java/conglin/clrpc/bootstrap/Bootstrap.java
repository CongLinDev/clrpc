package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.config.YamlPropertyConfigurer;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.cache.caffeine.CaffeineCacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

abstract public class Bootstrap{
    
    protected final boolean ENABLE_CACHE;
    protected final CacheManager<BasicRequest, BasicResponse> CACHE_MANAGER;

    protected final PropertyConfigurer CONFIGURER;

    public Bootstrap() {
        this(new YamlPropertyConfigurer());
    }

    public Bootstrap(String configFileName) {
        this(new YamlPropertyConfigurer(configFileName));
    }

    public Bootstrap(PropertyConfigurer configurer){
        this(configurer, configurer.getOrDefault("service.cache.enable", false));
    }

    public Bootstrap(PropertyConfigurer configurer, boolean enableCache) {
        this.CONFIGURER = configurer;
        this.ENABLE_CACHE = enableCache;
        if(ENABLE_CACHE){
            CACHE_MANAGER = new CaffeineCacheManager(configurer);
        }else{
            CACHE_MANAGER = null;
        }
    }
}