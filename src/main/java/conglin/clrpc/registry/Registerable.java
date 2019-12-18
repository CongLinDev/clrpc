package conglin.clrpc.registry;

/**
 * 可以注册的对象
 */
public interface Registerable {

    /**
     * 注册
     * 
     * @param key
     * @param data
     */
    void register(String key, String data);
}