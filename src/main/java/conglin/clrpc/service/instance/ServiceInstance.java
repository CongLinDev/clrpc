package conglin.clrpc.service.instance;

import conglin.clrpc.service.Service;
import conglin.clrpc.service.ServiceVersion;

public interface ServiceInstance extends Service {

    String INSTANCE_ADDRESS = "INSTANCE_ADDRESS";

    /**
     * 地址
     *
     * @return
     */
    String address();

    /**
     * 服务版本号
     *
     * @return
     */
    default ServiceVersion version() {
        return ServiceVersion.defaultVersion();
    }
}
