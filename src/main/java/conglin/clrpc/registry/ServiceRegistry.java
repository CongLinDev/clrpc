package conglin.clrpc.registry;

/**
 * 服务注册
 * 
 * 用于服务提供者注册到注册中心
 */
public interface ServiceRegistry {

    /**
     * 注册
     * 
     * @param type
     * @param key
     * @param value
     */
    void register(String type, String key, String value);

    /**
     * 取消注册
     * 
     * @param type
     * @param key
     */
    void unregister(String type, String key);
}