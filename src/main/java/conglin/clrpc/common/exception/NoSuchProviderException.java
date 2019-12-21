package conglin.clrpc.common.exception;

import conglin.clrpc.transport.message.BasicRequest;

/**
 * 找不到特定的Provider的时候抛出该异常
 */
public class NoSuchProviderException extends RequestException {

    private static final long serialVersionUID = -5999082213016050638L;

    public NoSuchProviderException(String address, BasicRequest request) {
        super(request, "There is no valid provider whose address is " + address);
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}