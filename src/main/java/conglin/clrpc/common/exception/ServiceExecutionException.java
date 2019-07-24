package conglin.clrpc.common.exception;

import conglin.clrpc.transfer.net.message.BasicRequest;

public class ServiceExecutionException extends RpcServiceException {

    private static final long serialVersionUID = 4259894135580173323L;

    public ServiceExecutionException(BasicRequest request, Exception exception) {
        super(request, exception.getMessage());  
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}