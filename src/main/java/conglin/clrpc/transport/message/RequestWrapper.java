package conglin.clrpc.transport.message;

import conglin.clrpc.common.Fallback;
import conglin.clrpc.router.instance.ServiceInstance;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RequestWrapper {

    protected RequestPayload request;

    protected Fallback fallback;

    protected Predicate<ServiceInstance> predicate;

    protected Consumer<ServiceInstance> beforeSendRequest;

    public RequestPayload getRequest() {
        return request;
    }

    public void setRequest(RequestPayload request) {
        this.request = request;
    }

    public Fallback getFallback() {
        return fallback;
    }

    public void setFallback(Fallback fallback) {
        this.fallback = fallback;
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
