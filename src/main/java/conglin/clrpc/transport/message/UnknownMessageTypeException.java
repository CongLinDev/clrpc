package conglin.clrpc.transport.message;

import conglin.clrpc.common.exception.RpcServiceException;

public class UnknownMessageTypeException extends RpcServiceException {

    private final int type;

    public UnknownMessageTypeException(int type) {
        super("Unknown message type=" + type);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
