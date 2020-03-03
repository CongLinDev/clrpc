package conglin.clrpc.transport.message;

/**
 * 系统消息
 */
public class SystemMessage extends Message {

    transient public static final int MESSAGE_TYPE = 0;

    private String command; // 指令

    public SystemMessage(Long messageId) {
        super(messageId);
    }

    public SystemMessage(SystemMessage message) {
        super(message);
        this.command = message.getCommand();
    }

    /**
     * 获取指令
     * 
     * @return command
     */
    public String getCommand() {
        return command;
    }

    /**
     * 设置指令
     * 
     * @param command the command to set
     */
    public void setCommand(String command) {
        this.command = command;
    }

    @Override
    public int messageType() {
        return MESSAGE_TYPE;
    }

    @Override
    public String toString() {
        return "SystemMessage [messageId=" + getMessageId() + ", command=" + command + "]";
    }
}