package conglin.clrpc.registry;

import java.util.Collection;
import java.util.function.BiConsumer;

import conglin.clrpc.common.Pair;

/**
 * 服务发现
 * 
 * 用于服务消费者监视 注册中心 中服务提供者的状态变更 以便及时通知服务消费者
 */
public interface ServiceDiscovery extends Registerable {
    /**
     * 发现服务
     * 
     * @param serviceName
     * @param updateMethod
     * @return
     */
    void discover(String serviceName, BiConsumer<String, Collection<Pair<String, String>>> updateMethod);
}