package conglin.clrpc.service.registry;

public interface ServiceRegistry{

    /**
     * 注册服务提供者
     * @param serviceName
     * @param data
     */
    void registerProvider(String serviceName, String data);
}