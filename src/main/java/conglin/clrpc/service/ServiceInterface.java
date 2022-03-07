package conglin.clrpc.service;

import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.strategy.FailStrategy;

public interface ServiceInterface<T> extends Service {

    /**
     * class 对象
     *
     * @return
     */
    Class<T> interfaceClass();

    /**
     * failStrategy
     * 
     * @return
     */
    FailStrategy failStrategy();

    /**
     * 超时时间
     * 
     * @return 超时阈值 单位为 ms
     */
    long timeoutThreshold();

    /**
     * instanceCondition
     * 
     * @return
     */
    InstanceCondition instanceCondition();
}
