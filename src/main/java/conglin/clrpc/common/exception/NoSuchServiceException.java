package conglin.clrpc.common.exception;

import conglin.clrpc.transfer.message.BasicRequest;

public class NoSuchServiceException extends RpcServiceException {

    private static final long serialVersionUID = -2704737758397975272L;

    public NoSuchServiceException(BasicRequest request){
        super(request, "There is no available service for your request.");
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}