package conglin.clrpc.common.registry;

/**
 * 服务发现
 * 
 * 用于服务消费者监视 注册中心 中服务提供者的状态变更 以便及时通知服务消费者
 */
public interface ServiceDiscovery extends ServiceRegistry {
    /**
     * 发现服务
     * 
     * @param serviceName
     * @param updater
     * @return
     */
    void discover(String serviceName, DiscoveryCallback updater);
}