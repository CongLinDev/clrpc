package conglin.clrpc.service;

import conglin.clrpc.common.config.PropertyConfigurer;

public interface ServiceObject extends Service {

    String OBJECT = "OBJECT";   // 对象

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
}
