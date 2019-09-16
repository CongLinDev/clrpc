package conglin.clrpc.transfer.handler.codec;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.codec.SerializationHandlerHolder;
import conglin.clrpc.transfer.message.TransactionRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class TransactionRequestEncoder extends MessageToByteEncoder<TransactionRequest> {

    
    private final SerializationHandler serializationHandler;

    public TransactionRequestEncoder(){
        super();
        serializationHandler = SerializationHandlerHolder.getHandler();
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