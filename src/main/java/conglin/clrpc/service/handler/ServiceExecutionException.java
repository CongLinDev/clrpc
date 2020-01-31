package conglin.clrpc.service.handler;

import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transport.message.BasicRequest;

public class ServiceExecutionException extends RpcServiceException {

    private static final long serialVersionUID = 7912835199025899934L;

    public ServiceExecutionException(String desc, Throwable throwable) {
        super(desc, throwable);
    }

    public ServiceExecutionException(BasicRequest request, Throwable throwable) {
        super("ServiceExecutionException: " + request.toString(), throwable);
    }
}