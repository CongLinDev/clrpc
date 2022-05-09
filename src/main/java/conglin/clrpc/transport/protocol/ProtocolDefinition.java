package conglin.clrpc.transport.protocol;

import conglin.clrpc.transport.message.Payload;

public interface ProtocolDefinition {
    /**
     * 版本
     * 
     * @return
     */
    int version();

    /**
     * payload type
     * 
     * @param payloadClass
     * @return
     * @throws UnknownPayloadTypeException
     */
    int getTypeByPayload(Class<? extends Payload> payloadClass) throws UnknownPayloadTypeException;

    /**
     * payload type
     * 
     * @param type
     * @return
     * @throws UnknownPayloadTypeException
     */
    Class<? extends Payload> getPayloadByType(int type) throws UnknownPayloadTypeException;

    /**
     * 设置 payload type
     * 
     * @param type
     * @param clazz
     */
    void setPayloadType(int type, Class<? extends Payload> clazz);
}
