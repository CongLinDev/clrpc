package conglin.clrpc.transfer.codec.protostuff;

import java.util.List;

import conglin.clrpc.transfer.codec.SerializationHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ProtostuffDecoder extends ByteToMessageDecoder{
    private SerializationHandler serializationHandler;
    private final Class<?> genericClass;

    public ProtostuffDecoder(Class<?> genericClass){
        serializationHandler = ProtostuffSerializationHandler.getInstance();
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if(in.readableBytes() <= 4) return;

        in.markReaderIndex();

        int dataLengh = in.readInt();
        if(dataLengh <= 0) return;
        if(in.readableBytes() < dataLengh){
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLengh];
        in.readBytes(data);
        Object obj = serializationHandler.deserialize(data, genericClass);
        out.add(obj);
    }
}