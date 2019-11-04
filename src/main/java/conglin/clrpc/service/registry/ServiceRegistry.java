package conglin.clrpc.service.registry;

import javax.security.auth.Destroyable;

public interface ServiceRegistry extends Destroyable {

    /**
     * 注册服务提供者
     * @param serviceName
     * @param data
     */
    void registerProvider(String serviceName, String data);
}