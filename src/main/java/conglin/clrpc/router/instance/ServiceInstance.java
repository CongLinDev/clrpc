package conglin.clrpc.router.instance;

import conglin.clrpc.service.Service;

public interface ServiceInstance extends Service {

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

    /**
     * 协议
     *
     * @return
     */
    default ServiceProtocol protocol() {
        return ServiceProtocol.clrpc;
    }
}
