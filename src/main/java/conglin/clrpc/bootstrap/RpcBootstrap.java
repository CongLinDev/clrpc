package conglin.clrpc.bootstrap;

import conglin.clrpc.common.config.JsonPropertyConfigurer;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.GlobalResourceManager;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.cache.caffeine.CaffeineCacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

abstract public class RpcBootstrap {

    protected final boolean ENABLE_CACHE;
    protected final CacheManager<BasicRequest, BasicResponse> CACHE_MANAGER;

    protected final PropertyConfigurer CONFIGURER;

    public RpcBootstrap() {
        this(JsonPropertyConfigurer.fromFile());
    }

    public RpcBootstrap(boolean enableCache) {
        this(JsonPropertyConfigurer.fromFile(), enableCache);
    }

    public RpcBootstrap(String configFileName) {
        this(JsonPropertyConfigurer.fromFile(configFileName));
    }

    public RpcBootstrap(PropertyConfigurer configurer) {
        this(configurer, configurer.getOrDefault("cache.enable", false));
    }

    public RpcBootstrap(String configFileName, boolean enableCache) {
        this(JsonPropertyConfigurer.fromFile(configFileName), false);
    }

    public RpcBootstrap(PropertyConfigurer configurer, boolean enableCache) {
        this.CONFIGURER = configurer;
        this.ENABLE_CACHE = enableCache;
        if (ENABLE_CACHE) {
            CACHE_MANAGER = new CaffeineCacheManager(configurer);
        } else {
            CACHE_MANAGER = null;
        }
    }

    /**
     * 准备
     */
    protected void start() {
        GlobalResourceManager.register();
    }

    /**
     * 销毁
     */
    protected void stop() {
        GlobalResourceManager.unregister();
    }

}