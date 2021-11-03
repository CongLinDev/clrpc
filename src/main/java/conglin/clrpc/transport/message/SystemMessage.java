package conglin.clrpc.transport.message;

import java.io.Serial;

/**
 * 系统消息
 */
public class SystemMessage extends Message {

    @Serial
    private static final long serialVersionUID = 1163465887291972266L;

    transient public static final int MESSAGE_TYPE = 0;

    private final Integer command; // 指令

    /**
     * 构造一个系统消息
     * 
     * @param messageId 消息ID
     * @param command   命令
     */
    public SystemMessage(Long messageId, Integer command) {
        super(messageId);
        this.command = command;
    }

    /**
     * 构造一个系统消息
     * 
     * @param message
     * 
     * @see #SystemMessage(Long, Integer)
     */
    public SystemMessage(SystemMessage message) {
        this(message.messageId(), message.command());
    }

    /**
     * 获取指令
     * 
     * @return command
     */
    final public Integer command() {
        return command;
    }

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    @Override
    public String toString() {
        return "SystemMessage [messageId=" + messageId() + ", command=" + command + "]";
    }
}