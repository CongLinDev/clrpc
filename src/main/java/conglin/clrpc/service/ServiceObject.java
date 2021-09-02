package conglin.clrpc.service;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.router.instance.ServiceInstance;

public interface ServiceObject extends Service {

    String OBJECT = "OBJECT";   // 对象
    String VERSION = "VERSION"; // 版本

    /**
     * 元信息
     *
     * @return
     */
    PropertyConfigurer metaInfo();

    /**
     * 对象
     *
     * @return
     */
    Object object();

    /**
     * 对象 class
     *
     * 默认返回 object().getClass()
     *
     * @return
     */
    default Class<?> objectClass() {
        return object().getClass();
    }

    /**
     * 服务版本
     *
     * @return
     */
    default ServiceVersion version() {
        return ServiceVersion.defaultVersion();
    }

    /**
     * 创建instance
     *
     * @param address
     * @return
     */
    ServiceInstance newServiceInstance(String address);
}
