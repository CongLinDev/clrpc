package conglin.clrpc.transfer.handler.codec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.transfer.message.Message;
import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class RpcDecoder extends ByteToMessageDecoder {
    private static final Logger log = LoggerFactory.getLogger(RpcDecoder.class);
    private static final SerializationHandler serializationHandler;

    static {
        String serializationHandlerName = ConfigParser.getOrDefault("service.codec.serialization-handler",
                "conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler");
        SerializationHandler handler = null;
        try {
            Class<?> clazz = Class.forName(serializationHandlerName);
            Method method = clazz.getMethod("getInstance");
            method.setAccessible(true);
            handler = SerializationHandler.class.cast(method.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            log.warn(e.getMessage()
                    + ". Loading 'conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler' rather than "
                    + serializationHandlerName);
        } finally {
            serializationHandler = (handler == null) ? ProtostuffSerializationHandler.getInstance() : handler;
        }
    }

    private final Class<? extends Message> genericClass;
    private final int MESSAGE_TYPE;

    private RpcDecoder(Class<? extends Message> genericClass) {
        this.genericClass = genericClass;

        int messageType = 0;
        try {
            messageType = genericClass.getDeclaredField("MESSAGE_TYPE").getInt(null);
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            log.error("Error message decoder. " + e.getMessage());
        }finally{
            MESSAGE_TYPE = messageType;
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() <= 8) return;
        in.markReaderIndex();
        
        int messageHeader = in.readInt();

        System.out.println(messageHeader);

        if(messageHeader != MESSAGE_TYPE){
            in.resetReaderIndex();
            return;
        }

        int dataLengh = in.readInt();
        if (dataLengh <= 0) return;
        System.out.println(dataLengh);
        
        if (in.readableBytes() < dataLengh) {
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLengh];
        in.readBytes(data);
        Object obj = serializationHandler.deserialize(data, genericClass);
        out.add(obj);
    }

    /**
     * 返回一个解码器
     * @param genericClass
     * @return
     */
    public static RpcDecoder getDecoder(Class<? extends Message> genericClass){
        return new RpcDecoder(genericClass);
    }
}