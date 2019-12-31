package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.cache.caffeine.CaffeineCacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

abstract public class RpcBootstrap{
    
    protected final boolean ENABLE_CACHE;
    protected final CacheManager<BasicRequest, BasicResponse> CACHE_MANAGER;

    protected final PropertyConfigurer CONFIGURER;

    public RpcBootstrap() {
        this(new JsonPropertyConfigurer());
    }

    public RpcBootstrap(String configFileName) {
        this(new JsonPropertyConfigurer(configFileName));
    }

    public RpcBootstrap(PropertyConfigurer configurer){
        this(configurer, configurer.getOrDefault("cache.enable", false));
    }

    public RpcBootstrap(PropertyConfigurer configurer, boolean enableCache) {
        this.CONFIGURER = configurer;
        this.ENABLE_CACHE = enableCache;
        if(ENABLE_CACHE){
            CACHE_MANAGER = new CaffeineCacheManager(configurer);
        }else{
            CACHE_MANAGER = null;
        }
    }
}