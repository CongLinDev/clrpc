package conglin.clrpc.router.instance;

public interface ServiceInstance {
    /**
     * 服务名称
     *
     * @return
     */
    String name();

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
