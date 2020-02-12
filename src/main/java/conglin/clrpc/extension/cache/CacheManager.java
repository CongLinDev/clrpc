package conglin.clrpc.extension.cache;

public interface CacheManager<K, V> {
    /**
     * 获取缓存
     * 
     * @param key
     * @return
     */
    V get(K key);

    /**
     * 是否存在缓存
     * 
     * @param key
     * @return
     */
    default boolean isExist(K key) {
        return get(key) != null;
    }

    /**
     * 加入缓存
     * 
     * @param key
     * @param value
     */
    void put(K key, V value);

    /**
     * 清空缓存
     */
    void clear();
}