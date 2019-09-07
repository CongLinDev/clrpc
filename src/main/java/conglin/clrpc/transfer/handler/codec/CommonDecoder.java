package conglin.clrpc.transfer.handler.codec;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.message.Message;
import conglin.clrpc.common.codec.protostuff.ProtostuffSerializationHandler;
import conglin.clrpc.common.codec.SerializationHandler;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class CommonDecoder extends LengthFieldBasedFrameDecoder {

    private static final Logger log = LoggerFactory.getLogger(CommonDecoder.class);


    //            ---------------------------------------------------------
    //  字节数    |     4     |     4     |                n              |
    //  解释      |  消息头   |  正文长度  |              正文             |
    //           ---------------------------------------------------------
    private static final int MESSAGE_HEADER_LENGTH = 4;
    private static final int MESSAGE_BODY_LENGTH_FIELD_LENGTH = 4;



    // 配置编码器
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

    public CommonDecoder(){
        this(65536, MESSAGE_HEADER_LENGTH, MESSAGE_BODY_LENGTH_FIELD_LENGTH, 0, 0, true);
    }

    /**
     * @param maxFrameLength  帧的最大长度
     * @param lengthFieldOffset length字段偏移的地址
     * @param lengthFieldLength length字段所占的字节长
     * @param lengthAdjustment 修改帧数据长度字段中定义的值，可以为负数 因为有时候我们习惯把头部记入长度,若为负数,则说明要推后多少个字段
     * @param initialBytesToStrip 解析时候跳过多少个长度
     * @param failFast 为true，当frame长度超过maxFrameLength时立即报TooLongFrameException异常，为false，读取完整个帧再报异
     */
    public CommonDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment,
            int initialBytesToStrip, boolean failFast) {
        super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip, failFast);
    }

    @Override
    protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
        ByteBuf byteBuf = (ByteBuf)super.decode(ctx, in);
        if(byteBuf == null) return null;
        int messageHeader = byteBuf.readInt();
        byte[] messageBody = new byte[byteBuf.readableBytes()];
        byteBuf.readBytes(messageBody);
        return decode(messageHeader, messageBody);
    }

    /**
     * 解码
     * @param messageHeader
     * @param messageBody
     * @return
     */
    protected Object decode(int messageHeader, byte[] messageBody){
        Object result = null;

        /**
         * 根据不同的消息类型，对消息进行解码
         * 
         * 消息类型占用一个字节
         */
        int messageType = messageHeader & Message.MESSAGE_TYPE_MASK;
        
        switch(messageType){
            case BasicRequest.MESSAGE_TYPE:
                result = serializationHandler.deserialize(messageBody, BasicRequest.class);
            case BasicResponse.MESSAGE_TYPE:
                result = serializationHandler.deserialize(messageBody, BasicResponse.class);
            default:
                log.error("Can not decode message type=" + messageType);
        }
        return result;
    }
}