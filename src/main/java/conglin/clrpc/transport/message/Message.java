package conglin.clrpc.transport.message;

import java.io.Serializable;

/**
 * 消息类
 * 
 * 具体的消息类必须覆盖 {@link Message#messageType()} 方法
 * 
 * 若将具体消息用于传输通信，则可以调用
 * {@link conglin.clrpc.global.GlobalMessageManager#setMessageClass(int, Class)}
 */
abstract public class Message implements Serializable {

    private static final long serialVersionUID = -7949510479931399141L;

    /**
     * 继承该类的子类，必须设定一个消息类型码 默认为4个比特位
     * 
     * 最低位为偶数则代表消息为请求消息 如 2 4 6 最低位为奇数则代表消息为回复消息 如 1 3 5
     * 
     * 其中 0 被系统消息占用
     */
    transient public static final int MESSAGE_TYPE_MASK = 0xf;

    /**
     * 返回信息类型
     * 
     * 每个具体的类型必须实现该方法
     * 
     * @return
     */
    abstract public int messageType();

    private final Long messageId;

    /**
     * 构造一个 Message 对象
     * 
     * @param messageId
     */
    public Message(Long messageId) {
        this.messageId = messageId;
    }

    /**
     * 构造一个 Message 对象
     * 
     * @param message
     * 
     * @see #Message(Long)
     */
    public Message(Message message) {
        this(message.messageId());
    }

    /**
     * 获得消息ID
     * 
     * @return the messageId
     */
    final public Long messageId() {
        return messageId;
    }

    @Override
    public String toString() {
        return "Message [messageId=" + messageId + "]";
    }
}