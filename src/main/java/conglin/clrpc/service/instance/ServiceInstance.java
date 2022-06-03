package conglin.clrpc.service.instance;

import conglin.clrpc.service.Service;
import conglin.clrpc.service.ServiceObject;
import conglin.clrpc.service.ServiceVersion;

public interface ServiceInstance extends Service {

    String INSTANCE_ADDRESS = "INSTANCE_ADDRESS";
    String INSTANCE_ID = "INSTANCE_ID";

    /**
     * instance id
     * 
     * @return
     */
    String id();

    /**
     * 地址
     *
     * @return
     */
    String address();

    /**
     * 服务对象
     * 
     * @return
     */
    ServiceObject<?> serviceObject();

    /**
     * 服务版本号
     *
     * @return
     */
    default ServiceVersion version() {
        return serviceObject().version();
    }

    /**
     * match
     * 
     * @param instance1
     * @param instance2
     * @return
     */
    static boolean match(ServiceInstance instance1, ServiceInstance instance2) {
        if (instance1 == null || instance2 == null)
            return false;
        return instance1.id().equals(instance2.id());
    }
}
