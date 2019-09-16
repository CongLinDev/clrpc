package conglin.clrpc.transfer.handler.codec;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.codec.SerializationHandlerHolder;
import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class BasicResponseEncoder extends MessageToByteEncoder<BasicResponse> {

    
    private final SerializationHandler serializationHandler;

    public BasicResponseEncoder(){
        super();
        serializationHandler = SerializationHandlerHolder.getHandler();
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