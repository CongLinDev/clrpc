package conglin.clrpc.extension.cache;

import java.util.Properties;

abstract public class AbstractCacheManager<K, V> implements CacheManager<K, V> {

    protected final int INITIAL_CAPACITY;
    protected final int MAX_SIZE;
    protected final int MAX_EXPIRE_TIME;

    public AbstractCacheManager(Properties properties) {
        INITIAL_CAPACITY = Integer.parseInt(properties.getProperty("extension.cache.initial-capacity", "16"));
        MAX_SIZE = Integer.parseInt(properties.getProperty("extension.cache.max-size", "10000"));
        MAX_EXPIRE_TIME = Integer.parseInt(properties.getProperty("extension.cache.max-expire-time", "10000"));
    }
}