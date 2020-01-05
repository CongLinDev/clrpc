package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.GlobalResourceManager;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.cache.caffeine.CaffeineCacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

abstract public class RpcBootstrap {

    protected final CacheManager<BasicRequest, BasicResponse> CACHE_MANAGER;

    protected final PropertyConfigurer CONFIGURER;

    public RpcBootstrap() {
        this(null);
    }

    public RpcBootstrap(PropertyConfigurer configurer) {
        if (configurer == null) {
            this.CONFIGURER = JsonPropertyConfigurer.fromFile(); // default configurer
        } else {
            this.CONFIGURER = configurer;
        }

        boolean enableCache = configurer.getOrDefault("cache.enable", false);
        if (enableCache) {
            CACHE_MANAGER = new CaffeineCacheManager(configurer);
        } else {
            CACHE_MANAGER = null;
        }
    }

    /**
     * 准备
     */
    protected void start() {
        GlobalResourceManager.manager().register();
    }

    /**
     * 销毁
     */
    protected void stop() {
        GlobalResourceManager.manager().unregister();
    }

}