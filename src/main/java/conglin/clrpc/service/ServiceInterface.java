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
     * instanceCondition
     * 
     * @return
     */
    InstanceCondition instanceCondition();
}
