package conglin.clrpc.service;

import java.util.Map;

public interface ServiceObject extends Service {
    /**
     * 元信息
     *
     * @return
     */
    Map<String, Object> metaInfo();

    /**
     * 元信息字符串
     *
     * @return
     */
    String metaInfoString();

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
