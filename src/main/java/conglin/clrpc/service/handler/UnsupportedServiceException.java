package conglin.clrpc.service.handler;

import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transport.message.BasicRequest;

public class UnsupportedServiceException extends RpcServiceException {

    private static final long serialVersionUID = 2977212781905153793L;

    public UnsupportedServiceException(BasicRequest request) {
        this(request.getServiceName());
    }

    public UnsupportedServiceException(String serviceName) {
        super("UnsupportedService: " + serviceName);
    }
}