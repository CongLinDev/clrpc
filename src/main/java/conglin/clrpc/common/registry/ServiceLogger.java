package conglin.clrpc.common.registry;

import conglin.clrpc.common.Calculatable;

/**
 * 服务日志
 */
public interface ServiceLogger {

    /**
     * 添加记录的对象
     * 
     * @param key
     * @param calculatable
     */
    void put(String key, Calculatable<?> calculatable);

    /**
     * 移出记录的对象
     * 
     * @param key
     */
    void remove(String key);
}