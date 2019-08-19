package conglin.clrpc.common.exception;

import conglin.clrpc.transfer.message.BasicRequest;

public class NoSuchProviderException extends RpcServiceException {

    private static final long serialVersionUID = -5999082213016050638L;

    public NoSuchProviderException(String address, BasicRequest request) {
        super(request, "There is no valid provider whose address is " + address);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}