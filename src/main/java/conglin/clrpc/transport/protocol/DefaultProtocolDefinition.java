package conglin.clrpc.transport.protocol;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.RequestPayload;
import conglin.clrpc.transport.message.ResponsePayload;
import conglin.clrpc.transport.message.SystemPayload;

public class DefaultProtocolDefinition implements ProtocolDefinition {

    private final int version;    
    private final Class<?>[] payloadClasses;

    public DefaultProtocolDefinition(int version, int capacity) {
        this.version = version;
        payloadClasses = new Class<?>[capacity];
        initDefaultMessageType();
    }

    public DefaultProtocolDefinition() {
        this(0, Payload.PAYLOAD_TYPE_MASK + 1);
    }

    /**
     * 初始化默认的消息类型
     */
    private void initDefaultMessageType() {
        setPayloadType(SystemPayload.PAYLOAD_TYPE, SystemPayload.class);
        setPayloadType(RequestPayload.PAYLOAD_TYPE, RequestPayload.class);
        setPayloadType(ResponsePayload.PAYLOAD_TYPE, ResponsePayload.class);
    }

    @Override
    public int version() {
        return version;
    }

  
    @Override
    public void setPayloadType(int type, Class<? extends Payload> clazz) {
        if (payloadClasses[type] == null) {
            payloadClasses[type] = clazz;
            return;
        }
        if (payloadClasses[type] == clazz) return;
        throw new IllegalArgumentException("Message type=" + type + " has been used.");
    }

    /**
     * 寻找可用的消息类型。若找不到返回 -1
     * 
     * @return
     */
    public int availablePayloadType() {
        for (int index = 0; index < payloadClasses.length; index++) {
            if (payloadClasses[index] == null)
                return index;
        }
        return -1;
    }

    /**
     * 可用的消息类型列表
     * 
     * @return
     */
    public List<Class<?>> listPayloadClasses() {
        return Stream.of(payloadClasses).filter(Objects::nonNull).toList();
    }

    /**
     * 消息类型容量
     * 
     * @return
     */
    public int capacity() {
        return payloadClasses.length;
    }

    @Override
    public int getTypeByPayload(Class<? extends Payload> payloadClass) throws UnknownPayloadTypeException {
        for (int index = 0; index < payloadClasses.length; index++) {
            if (payloadClass.equals(payloadClasses[index])) {
                return index;
            }
        }
        throw new UnknownPayloadTypeException(payloadClass);
    }

    @Override
    public Class<? extends Payload> getPayloadByType(int type) throws UnknownPayloadTypeException {
        @SuppressWarnings("unchecked")
        Class<? extends Payload> clazz = (Class<? extends Payload>) payloadClasses[type];
        if (clazz == null)
            throw new UnknownPayloadTypeException(type);
        return clazz;
    }
    
}
