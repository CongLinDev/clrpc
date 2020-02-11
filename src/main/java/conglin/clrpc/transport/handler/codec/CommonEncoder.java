package conglin.clrpc.transport.handler.codec;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.transport.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class CommonEncoder extends MessageToByteEncoder<Message> {

    private final SerializationHandler serializationHandler;

    public CommonEncoder(SerializationHandler serializationHandler) {
        super();
        this.serializationHandler = serializationHandler;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
        byte[] data = serializationHandler.serialize(msg);
        int messageHeader = msg.messageType();
        out.writeInt(messageHeader);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}