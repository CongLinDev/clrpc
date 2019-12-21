package conglin.clrpc.transport.handler.codec;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.transport.message.BasicRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class BasicRequestEncoder extends MessageToByteEncoder<BasicRequest> {

    private final SerializationHandler serializationHandler;

    public BasicRequestEncoder(SerializationHandler serializationHandler) {
        super();
        this.serializationHandler = serializationHandler;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, BasicRequest msg, ByteBuf out) throws Exception {
        byte[] data = serializationHandler.serialize(msg);

        int messageHeader = BasicRequest.MESSAGE_TYPE;

        out.writeInt(messageHeader);
        out.writeInt(data.length);
        out.writeBytes(data);
    }

}