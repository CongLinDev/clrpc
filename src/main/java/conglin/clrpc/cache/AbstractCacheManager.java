package conglin.clrpc.cache;

import conglin.clrpc.common.config.ConfigParser;

abstract public class AbstractCacheManager<K, V>{

    protected final int INITIAL_CAPACITY;
    protected final int MAX_SIZE;
    protected final int MAX_EXPIRE_TIME;

    public AbstractCacheManager(){
        INITIAL_CAPACITY = ConfigParser.getOrDefault("cache.initial-capacity", 16);
        MAX_SIZE = ConfigParser.getOrDefault("cache.max-size", 10000);
        MAX_EXPIRE_TIME = ConfigParser.getOrDefault("cache.max-expire-time", 1 << 20);
    }


    abstract public void put(K key, V value);

    abstract public V get(K key);
}