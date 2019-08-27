package conglin.clrpc.cache;

public interface CacheManager<K, V>{
    /**
     * 获取缓存
     * @param key
     * @return
     */
    V get(K key);

    /**
     * 加入缓存
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     * 清空缓存
     */
    void clear();
}