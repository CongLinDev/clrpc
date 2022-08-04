package conglin.clrpc.service.registry;

import java.util.Properties;

public interface ServiceRegistryFactory {
    /**
     * 获取 registry
     * 
     * @param properties
     * @return
     */
    ServiceRegistry get(Properties properties);
}
