package conglin.clrpc.service.discovery;

/**
 * 扫描当前工程下的所有注解为
 * {@link RpcService} 的接口
 * 并将其注册到Zookeeper上
 */
public interface ServiceDiscovery{
    /**
     * 发现服务
     * @return
     */
    String discover();

    /**
     * 关闭服务
     */
    void stop() throws InterruptedException;
}