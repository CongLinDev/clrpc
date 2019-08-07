package conglin.clrpc.common.exception;

import conglin.clrpc.transfer.net.message.BasicRequest;

public class NoSuchServerException extends RpcServiceException {

    private static final long serialVersionUID = -5999082213016050638L;

    public NoSuchServerException(String address, BasicRequest request) {
        super(request, "There is no valid server whose address is " + address);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}