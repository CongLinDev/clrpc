package conglin.clrpc.global;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.CacheableResponse;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.TransactionRequest;

/**
 * 使用数组的下标和数组来控制消息的类型
 */
public class GlobalMessageManager {

    private final Class<?> messageClasses[];

    private GlobalMessageManager() {
        messageClasses = new Class[Message.MESSAGE_TYPE_MASK + 1];
        initDefaultMessageType();
    }

    /**
     * 初始化默认的消息类型
     */
    private void initDefaultMessageType() {
        setMessageClass(Message.MESSAGE_TYPE, Message.class);
        setMessageClass(BasicResponse.MESSAGE_TYPE, BasicResponse.class);
        setMessageClass(BasicRequest.MESSAGE_TYPE, BasicRequest.class);
        setMessageClass(CacheableResponse.MESSAGE_TYPE, CacheableResponse.class);
        setMessageClass(TransactionRequest.MESSAGE_TYPE, TransactionRequest.class);
    }

    /**
     * 获取manager对象
     * 
     * @return
     */
    public static GlobalMessageManager manager() {
        return SingletonHolder.INSTANCE;
    }

    private static class SingletonHolder {
        private static final GlobalMessageManager INSTANCE = new GlobalMessageManager();
    }

    /**
     * 获取消息类对象
     * 
     * @param messageType
     * @return
     */
    public Class<? extends Message> getMessageClass(int messageType) {
        @SuppressWarnings("unchecked")
        Class<? extends Message> clazz = (Class<? extends Message>) messageClasses[messageType];
        if (clazz == null)
            throw new NullPointerException();
        return clazz;
    }

    /**
     * 设置消息类对象
     * 
     * @param messageType
     * @param clazz
     */
    public void setMessageClass(int messageType, Class<? extends Message> clazz) {
        if (messageClasses[messageType] != null)
            throw new IllegalArgumentException("Message type=" + messageType + " has been used.");
        messageClasses[messageType] = clazz;
    }

    /**
     * 设置消息类对象
     * 
     * @param clazz
     */
    public void setMessageClass(Class<? extends Message> clazz) {
        try {
            int messageType = clazz.getDeclaredField("MESSAGE_TYPE").getInt(null);
            setMessageClass(messageType, clazz);
        } catch (IllegalAccessException | NoSuchFieldException | SecurityException e) {
            new IllegalArgumentException("Consider adding a field 'MESSAGE_TYPE' in class " + clazz);
        }
    }

    /**
     * 可用的消息类型列表
     * 
     * @return
     */
    public Collection<Class<?>> messageTypes() {
        return Stream.of(messageClasses).filter(Objects::nonNull).collect(Collectors.toList());
    }
}