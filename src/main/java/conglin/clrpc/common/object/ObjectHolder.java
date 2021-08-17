package conglin.clrpc.common.object;

public interface ObjectHolder<T> {
    /**
     * put object
     *
     * @param key
     * @param value
     */
    void put(T key, Object value);

    /**
     * get object
     *
     * @param key
     * @return
     */
    Object get(T key);

    /**
     * get object 默认转换类型
     *
     * @param key
     * @param <V>
     * @return
     */
    @SuppressWarnings("unchecked")
    default <V> V getWith(T key) {
        return (V) get(key);
    }
}
