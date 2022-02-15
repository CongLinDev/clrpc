package conglin.clrpc.transport.protocol;

import conglin.clrpc.common.exception.ServiceException;
import conglin.clrpc.transport.message.Payload;

public class UnknownPayloadTypeException extends ServiceException {

    private final int type;
    private final Class<? extends Payload> payloadClass;

    public UnknownPayloadTypeException(int type) {
        super("Unknown payload type=" + type + ". It usually happends in phase 'DECODE'. Consider register type by ProtocolDefinition.");
        this.type = type;
        this.payloadClass = null;
    }

    public UnknownPayloadTypeException(Class<? extends Payload> payloadClass) {
        super("Unknown payload class=" + payloadClass.getName() + ". It usually happends in phase 'ENCODE'. Consider register type by ProtocolDefinition.");
        this.type = -1;
        this.payloadClass = payloadClass;
    }

    public int getType() {
        return type;
    }

    public Class<? extends Payload> getPayloadClass() {
        return payloadClass;
    }
}
