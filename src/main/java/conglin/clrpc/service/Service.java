package conglin.clrpc.service;

public interface Service {

    String SERVICE_NAME = "SERVICE_NAME";
    String VERSION = "VERSION"; // 版本

    /**
     * 服务名称
     *
     * @return
     */
    String name();

    /**
     * 服务版本
     *
     * @return
     */
    default ServiceVersion version() {
        return ServiceVersion.defaultVersion();
    }
}
