package conglin.clrpc.transport.handler.codec;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.transport.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <pre>
 *  -----------------------------------
 *  | 字节数 |   1   |    4    |    n   |
 *  |  解释 | 消息头 | 正文长度 |   正文  |
 *  -----------------------------------
 * </pre>
 */
public class CommonEncoder extends MessageToByteEncoder<Message> {

    private final SerializationHandler serializationHandler;

    public CommonEncoder(SerializationHandler serializationHandler) {
        super();
        this.serializationHandler = serializationHandler;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        int messageHeader = msg.messageType();
        byte[] data = serializationHandler.serialize(msg);
        out.writeByte(messageHeader);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}