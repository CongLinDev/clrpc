package conglin.clrpc.transport.handler.codec;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class BasicResponseEncoder extends MessageToByteEncoder<BasicResponse> {

    private final SerializationHandler serializationHandler;

    public BasicResponseEncoder(SerializationHandler serializationHandler) {
        super();
        this.serializationHandler = serializationHandler;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, BasicResponse msg, ByteBuf out) throws Exception {
        byte[] data = serializationHandler.serialize(msg);

        int messageHeader = BasicResponse.MESSAGE_TYPE;

        out.writeInt(messageHeader);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}