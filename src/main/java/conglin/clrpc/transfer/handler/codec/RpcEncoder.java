package conglin.clrpc.transfer.handler.codec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ConfigParser;
import conglin.clrpc.transfer.message.Message;
import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder{
    
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

    private RpcEncoder(Class<? extends Message> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){
            byte[] data = serializationHandler.serialize(msg);
            System.out.println(data.length);
            int messageHeader = genericClass.getDeclaredField("MESSAGE_TYPE").getInt(msg);
            System.out.println("send message type=" + messageHeader);
            out.writeInt(messageHeader);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

    /**
     * 返回一个编码器
     * @param genericClass
     * @return
     */
    public static RpcEncoder getEncoder(Class<? extends Message> genericClass){
        return new RpcEncoder(genericClass);
    }
}