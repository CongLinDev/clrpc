package conglin.clrpc.transport.message;

/**
 * 消息类
 * 
 * 具体的消息类必须实现 {@link Message#messageType()} 方法
 * 以及 {@link Message#MESSAGE_TYPE} 域
 */
abstract public class Message {

    /**
     * 继承该类的子类，必须设定一个消息类型码 默认为1个字节
     * 
     * 最低位为偶数则代表消息为请求消息 如 2 4 6 最低位为奇数则代表消息为回复消息 如 1 3 5
     * 
     * 其中 0 被抽象消息占用 7 被保留
     */
    transient public static final int MESSAGE_TYPE_MASK = 0x7;
    transient public static final int MESSAGE_TYPE = 0;
    
    /**
     * 返回信息类型
     * 
     * 每个具体的类型必须实现该方法
     * 
     * @return
     */
    public int messageType() {
        return MESSAGE_TYPE;
    }

    private final Long requestId;

    public Message(Long requestId) {
        this.requestId = requestId;
    }

    public Message(Message message) {
        this.requestId = message.getRequestId();
    }

    /**
     * 获得请求ID
     * 
     * @return the requestId
     */
    public Long getRequestId() {
        return requestId;
    }

}