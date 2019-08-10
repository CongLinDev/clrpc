package conglin.clrpc.service.discovery;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 发现服务
 * 用于监视 ZooKeeper 中服务提供者的状态变更
 * 以便及时通知服务消费者
 */
public interface ServiceDiscovery{
    /**
     * 发现服务
     * @param serviceName
     * @param updateMethod
     * @return
     */
    void discover(String serviceName, BiConsumer<String, List<String>> updateMethod);

    /**
     * 关闭服务
     */
    void stop();

    /**
     * 注册消费者
     * @param serviceName
     * @param data
     */
    void registerConsumer(String serviceName, String data);
}