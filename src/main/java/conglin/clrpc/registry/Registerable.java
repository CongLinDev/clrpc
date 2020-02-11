package conglin.clrpc.registry;

/**
 * 可以注册的对象
 */
public interface Registerable {

    /**
     * 注册
     * 
     * @param key
     */
    void register(String key);

    /**
     * 注册
     * 
     * @param key
     * @param data
     */
    void register(String key, String data);

    /**
     * 取消注册
     * 
     * @param key
     */
    void unregister(String key);
}