package conglin.clrpc.transfer.codec.protostuff;

import conglin.clrpc.transfer.codec.SerializationHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtostuffEncoder extends MessageToByteEncoder{

    private SerializationHandler serializationHandler;
    private final Class<?> genericClass;

    public ProtostuffEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
        serializationHandler = ProtostuffSerializationHandler.getInstance();
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){

            byte[] data = serializationHandler.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }
}