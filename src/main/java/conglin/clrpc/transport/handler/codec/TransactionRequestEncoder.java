package conglin.clrpc.transport.handler.codec;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.transport.message.TransactionRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TransactionRequestEncoder extends MessageToByteEncoder<TransactionRequest> {

    private final SerializationHandler serializationHandler;

    public TransactionRequestEncoder(SerializationHandler serializationHandler) {
        super();
        this.serializationHandler = serializationHandler;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, TransactionRequest msg, ByteBuf out) throws Exception {
        byte[] data = serializationHandler.serialize(msg);

        int messageHeader = TransactionRequest.MESSAGE_TYPE;

        out.writeInt(messageHeader);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}