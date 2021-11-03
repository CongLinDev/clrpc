package conglin.clrpc.transport.message;

import conglin.clrpc.common.Fallback;
import conglin.clrpc.router.instance.ServiceInstance;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class RequestWrapper {

    protected BasicRequest request;

    protected Fallback fallback;

    protected Predicate<ServiceInstance> predicate;

    protected Consumer<ServiceInstance> beforeSendRequest;

    public BasicRequest getRequest() {
        return request;
    }

    public void setRequest(BasicRequest request) {
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
