package conglin.clrpc.service;

import java.util.Map;

public interface ServiceObject<T> extends Service {

    String OBJECT = "OBJECT";           // 对象类名
    String INTERFACE = "INTERFACE";     // 接口类名

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
     * 接口 class
     *
     * @return
     */
    Class<T> interfaceClass();
}
