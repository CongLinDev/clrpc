package conglin.clrpc.common;

public interface Registerable {
    
    /**
     * 注册
     * @param key
     * @param data
     */
    void register(String key, String data);
}