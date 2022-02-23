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
}
