package conglin.clrpc.service.discovery;

import java.util.List;
import java.util.function.BiConsumer;

/**
 * 扫描当前工程下的所有注解为 {@link RpcService} 的接口 并将其注册到Zookeeper上
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