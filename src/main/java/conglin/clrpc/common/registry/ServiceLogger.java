package conglin.clrpc.common.registry;

import conglin.clrpc.common.Calculable;

/**
 * 服务日志
 */
public interface ServiceLogger {

    /**
     * 添加记录的对象
     * 
     * @param key
     * @param calculable
     */
    void put(String key, Calculable<?> calculable);

    /**
     * 移出记录的对象
     * 
     * @param key
     */
    void remove(String key);
}