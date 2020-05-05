package conglin.clrpc.global;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.CacheableResponse;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.SystemMessage;
import conglin.clrpc.transport.message.TransactionRequest;

/**
 * 全局的消息类型管理器
 * 
 * 使用数组的下标和数组来控制消息的类型
 */
public class GlobalMessageManager {

    private final Class<?> messageClasses[];

    private GlobalMessageManager(int capacity) {
        messageClasses = new Class[capacity];
        initDefaultMessageType();
    }

    private GlobalMessageManager() {
        this(Message.MESSAGE_TYPE_MASK + 1);
    }

    /**
     * 初始化默认的消息类型
     */
    private void initDefaultMessageType() {
        setMessageClass(SystemMessage.MESSAGE_TYPE, SystemMessage.class);
        setMessageClass(BasicResponse.MESSAGE_TYPE, BasicResponse.class);
        setMessageClass(BasicRequest.MESSAGE_TYPE, BasicRequest.class);
        setMessageClass(CacheableResponse.MESSAGE_TYPE, CacheableResponse.class);
        setMessageClass(TransactionRequest.MESSAGE_TYPE, TransactionRequest.class);
    }

    /**
     * 获取 GlobalMessageManager 对象
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
            throw new NullPointerException("Unkonwn message type=" + messageType);
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
     * 获取消息类型码
     * 
     * @param clazz
     * @return
     */
    public int getMessageType(Class<? extends Message> clazz) {
        if (clazz == null)
            throw new NullPointerException("Class is null");
        for (int index = 0; index < messageClasses.length; index++) {
            if (clazz.equals(messageClasses[index])) {
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
    public int availableMessageType() {
        for (int index = 0; index < messageClasses.length; index++) {
            if (messageClasses[index] == null)
                return index;
        }
        return -1;
    }

    /**
     * 可用的消息类型列表
     * 
     * @return
     */
    public Collection<Class<?>> listMessageClasses() {
        return Stream.of(messageClasses).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * 消息类型容量
     * 
     * @return
     */
    public int capacity() {
        return messageClasses.length;
    }
}