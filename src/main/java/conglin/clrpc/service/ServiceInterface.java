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
     * failStrategyClass
     * 
     * @return
     */
    Class<? extends FailStrategy> failStrategyClass();

    /**
     * instanceCondition
     * 
     * @return
     */
    InstanceCondition instanceCondition();
}
