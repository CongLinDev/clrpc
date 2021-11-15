package conglin.clrpc.transport.message;

import conglin.clrpc.common.exception.RpcServiceException;

public class UnknownPayloadTypeException extends RpcServiceException {

    private final int type;

    public UnknownPayloadTypeException(int type) {
        super("Unknown payload type=" + type);
        this.type = type;
    }

    public int getType() {
        return type;
    }
}
