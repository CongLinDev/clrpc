package conglin.clrpc.transport.message;

import conglin.clrpc.common.Fallback;

public class RequestWrapper {

    protected BasicRequest request;

    protected Fallback fallback;

    protected String remoteAddress;

    protected Runnable beforeSendRequest;

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

    public String getRemoteAddress() {
        return remoteAddress;
    }

    public void setRemoteAddress(String remoteAddress) {
        this.remoteAddress = remoteAddress;
    }

    public Runnable getBeforeSendRequest() {
        return beforeSendRequest;
    }

    public void setBeforeSendRequest(Runnable beforeSendRequest) {
        this.beforeSendRequest = beforeSendRequest;
    }
}
