package conglin.clrpc.transport.message;

import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.ServiceInstance;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RequestWrapper {

    protected RequestPayload request;

    protected Class<? extends FailStrategy> failStrategyClass;

    protected Predicate<ServiceInstance> predicate;

    protected Consumer<ServiceInstance> beforeSendRequest;

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

    public Predicate<ServiceInstance> getPredicate() {
        return predicate;
    }

    public void setPredicate(Predicate<ServiceInstance> predicate) {
        this.predicate = predicate;
    }

    public Consumer<ServiceInstance> getBeforeSendRequest() {
        return beforeSendRequest;
    }

    public void setBeforeSendRequest(Consumer<ServiceInstance> beforeSendRequest) {
        this.beforeSendRequest = beforeSendRequest;
    }
}
