package conglin.clrpc.extension.cache;

import conglin.clrpc.common.config.PropertyConfigurer;

abstract public class AbstractCacheManager<K, V> implements CacheManager<K, V> {

    protected final int INITIAL_CAPACITY;
    protected final int MAX_SIZE;
    protected final int MAX_EXPIRE_TIME;

    public AbstractCacheManager(PropertyConfigurer configurer) {
        INITIAL_CAPACITY = configurer.getOrDefault("cache.initial-capacity", 16);
        MAX_SIZE = configurer.getOrDefault("cache.max-size", 10000);
        MAX_EXPIRE_TIME = configurer.getOrDefault("cache.max-expire-time", 1 << 20);
    }
}