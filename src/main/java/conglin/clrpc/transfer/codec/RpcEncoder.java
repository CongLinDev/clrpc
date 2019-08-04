package conglin.clrpc.transfer.codec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.transfer.codec.SerializationHandler;
import conglin.clrpc.transfer.codec.protostuff.ProtostuffSerializationHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class RpcEncoder extends MessageToByteEncoder{
    
    private static final Logger log = LoggerFactory.getLogger(RpcDecoder.class);
    private static final SerializationHandler serializationHandler;

    static {
        String serializationHandlerName = ConfigParser.getOrDefault("service.codec.serialization-handler",
                "conglin.clrpc.transfer.codec.protostuff.ProtostuffSerializationHandler");
        SerializationHandler handler = null;
        try {
            Class<?> clazz = Class.forName(serializationHandlerName);
            Method method = clazz.getMethod("getInstance");
            method.setAccessible(true);
            handler = SerializationHandler.class.cast(method.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            log.warn(e.getMessage()
                    + ". Loading 'conglin.clrpc.transfer.codec.protostuff.ProtostuffSerializationHandler' rather than "
                    + serializationHandlerName);
        } finally {
            serializationHandler = (handler == null) ? ProtostuffSerializationHandler.getInstance() : handler;
        }
    }

    private final Class<?> genericClass;

    private RpcEncoder(Class<?> genericClass){
        this.genericClass = genericClass;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if(genericClass.isInstance(msg)){

            byte[] data = serializationHandler.serialize(msg);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

    /**
     * 返回一个编码器
     * @param genericClass
     * @return
     */
    public static RpcEncoder getEncoder(Class<?> genericClass){
        return new RpcEncoder(genericClass);
    }
}