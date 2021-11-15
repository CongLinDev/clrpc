package conglin.clrpc.global;

import conglin.clrpc.transport.message.*;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 全局的消息类型管理器
 * 
 * 使用数组的下标和数组来控制消息的类型
 */
public class GlobalPayloadManager {

    private final Class<?>[] payloadClasses;

    private GlobalPayloadManager(int capacity) {
        payloadClasses = new Class[capacity];
        initDefaultMessageType();
    }

    private GlobalPayloadManager() {
        this(Payload.PAYLOAD_TYPE_MASK + 1);
    }

    /**
     * 初始化默认的消息类型
     */
    private void initDefaultMessageType() {
        setPayloadClass(SystemPayload.PAYLOAD_TYPE, SystemPayload.class);
        setPayloadClass(RequestPayload.PAYLOAD_TYPE, RequestPayload.class);
        setPayloadClass(ResponsePayload.PAYLOAD_TYPE, ResponsePayload.class);
    }

    /**
     * 获取 GlobalMessageManager 对象
     * 
     * @return
     */
    public static GlobalPayloadManager manager() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final GlobalPayloadManager INSTANCE = new GlobalPayloadManager();
    }

    /**
     * 获取消息类对象
     * 
     * @param messageType
     * @return
     * @throws UnknownPayloadTypeException
     */
    public Class<? extends Payload> getPayloadClass(int messageType) throws UnknownPayloadTypeException {
        @SuppressWarnings("unchecked")
        Class<? extends Payload> clazz = (Class<? extends Payload>) payloadClasses[messageType];
        if (clazz == null)
            throw new UnknownPayloadTypeException(messageType);
        return clazz;
    }

    /**
     * 设置消息类对象
     * 
     * @param messageType
     * @param clazz
     */
    public void setPayloadClass(int messageType, Class<? extends Payload> clazz) {
        if (payloadClasses[messageType] == null) {
            payloadClasses[messageType] = clazz;
            return;
        }
        if (payloadClasses[messageType] == clazz) return;
        throw new IllegalArgumentException("Message type=" + messageType + " has been used.");
    }

    /**
     * 获取消息类型码
     * 
     * @param clazz
     * @return
     */
    public int getPayloadType(Class<? extends Payload> clazz) {
        if (clazz == null)
            throw new NullPointerException("Class is null");
        for (int index = 0; index < payloadClasses.length; index++) {
            if (clazz.equals(payloadClasses[index])) {
                return index;
            }
        }
        return -1;
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
    public Collection<Class<?>> listPayloadClasses() {
        return Stream.of(payloadClasses).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 消息类型容量
     * 
     * @return
     */
    public int capacity() {
        return payloadClasses.length;
    }
}