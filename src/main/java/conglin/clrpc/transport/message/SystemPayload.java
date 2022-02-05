package conglin.clrpc.transport.message;

import java.io.Serial;
import java.io.Serializable;

public class SystemPayload implements Payload, Serializable {

    @Serial
    private static final long serialVersionUID = 1163465887291972266L;

    transient public static final int PAYLOAD_TYPE = 0;

    private final Integer command; // 指令

    /**
     * 构造一个系统消息
     *
     * @param command   命令
     */
    public SystemPayload(Integer command) {
        this.command = command;
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
    public String toString() {
        return "SystemPayload [command=" + command + "]";
    }
}
