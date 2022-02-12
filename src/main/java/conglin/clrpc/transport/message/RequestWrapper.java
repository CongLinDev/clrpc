package conglin.clrpc.transport.message;

import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;

import java.util.function.Consumer;

public class RequestWrapper {

    protected RequestPayload request;

    protected Class<? extends FailStrategy> failStrategyClass;

    protected InstanceCondition instanceCondition;

    protected Consumer<ServiceInstance> instanceConsumer;

    public RequestPayload getRequest() {
        return request;
    }

    public void setRequest(RequestPayload request) {
        this.request = request;
    }

    /**
     * @return the failStrategyClass
     */
    public Class<? extends FailStrategy> getFailStrategyClass() {
        return failStrategyClass;
    }

    /**
     * @param failStrategyClass the failStrategyClass to set
     */
    public void setFailStrategyClass(Class<? extends FailStrategy> failStrategyClass) {
        this.failStrategyClass = failStrategyClass;
    }

    /**
     * @return the instanceCondition
     */
    public InstanceCondition getInstanceCondition() {
        return instanceCondition;
    }
    
    /**
     * @param instanceCondition the instanceCondition to set
     */
    public void setInstanceCondition(InstanceCondition instanceCondition) {
        this.instanceCondition = instanceCondition;
    }

    public Consumer<ServiceInstance> getInstanceConsumer() {
        return instanceConsumer;
    }

    public void setInstanceConsumer(Consumer<ServiceInstance> instanceConsumer) {
        this.instanceConsumer = instanceConsumer;
    }
}
