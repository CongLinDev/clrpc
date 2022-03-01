package conglin.clrpc.service;

import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.condition.InstanceCondition;

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
