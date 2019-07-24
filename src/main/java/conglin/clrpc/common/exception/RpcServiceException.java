package conglin.clrpc.common.exception;

import conglin.clrpc.transfer.net.message.BasicRequest;

public class RpcServiceException extends RuntimeException {

    private static final long serialVersionUID = 1829465701565961733L;

    protected final BasicRequest request;

    protected final String description;

    public RpcServiceException(BasicRequest request, String description){
        this.request = request;
        this.description = description;
    }

    @Override
    public String getMessage(){
        StringBuilder s = new StringBuilder(description);
        s.append("[serviceName=").append(request.getServiceName())
            .append(", methodName=").append(request.getMethodName())
            .append(", requestId=").append(request.getRequestId()).append("]");
        return s.toString();
    }

    public String getDescription(){
        return description;
    }
}