package conglin.clrpc.service;

import conglin.clrpc.common.Fallback;

public interface ServiceInterface<T> extends Service {

    /**
     * class 对象
     *
     * @return
     */
    Class<T> interfaceClass();

    /**
     * fallback
     *
     * @return
     */
    Fallback fallback();
}
