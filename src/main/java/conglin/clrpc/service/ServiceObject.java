package conglin.clrpc.service;

import java.util.Map;

public interface ServiceObject<T> extends Service {

    String OBJECT = "OBJECT";   // 对象

    /**
     * 元信息
     *
     * @return
     */
    Map<String, String> metaInfo();

    /**
     * 对象
     *
     * @return
     */
    T object();

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
