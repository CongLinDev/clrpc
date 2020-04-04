package conglin.clrpc.transport.handler.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.global.GlobalMessageManager;
import conglin.clrpc.transport.handler.codec.RpcProtocolCodec.RpcProtocolDecoder;
import conglin.clrpc.transport.handler.codec.RpcProtocolCodec.RpcProtocolEncoder;
import conglin.clrpc.transport.message.Message;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * <pre>
 *  ---------------------------------------
 *  | 字节数 |   1   |    4     |    n     |
 *  |  解释 | 消息头 | 消息体长度 |   消息体  |
 *  --------------------------------------
 * 
 * </pre>
 * 
 * 消息头中高4个bit代表协议类型，低4个bit代表消息类型
 */

public class RpcProtocolCodec extends CombinedChannelDuplexHandler<RpcProtocolDecoder, RpcProtocolEncoder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProtocolCodec.class);

    // 消息类型管理
    private static final GlobalMessageManager manager = GlobalMessageManager.manager();

    private final SerializationHandler serializationHandler;

    public RpcProtocolCodec(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        init(new RpcProtocolDecoder(), new RpcProtocolEncoder());
    }

    /**
     * 解析获取消息类型
     * 
     * @param header
     * @return
     */
    protected int resolveMessageType(byte header) {
        return header & Message.MESSAGE_TYPE_MASK;
    }

    /**
     * 获取协议类型
     * 
     * @param header
     * @return
     */
    protected int resolveMessageProtocol(byte header) {
        return header >> 4;
    }

    /**
     * 编码器
     */
    class RpcProtocolEncoder extends MessageToByteEncoder<Message> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            int messageHeader = msg.messageType();
            byte[] data = serializationHandler.serialize(msg);
            out.writeByte(messageHeader);
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

    /**
     * 解码器
     */
    class RpcProtocolDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (in.readableBytes() <= 5)
                return;
            in.markReaderIndex();

            byte messageHeader = in.readByte();
            int dataLengh = in.readInt();

            if (dataLengh <= 0) {
                LOGGER.error("Error format message whose length is negative.");
                return;
            }

            if (in.readableBytes() < dataLengh) {
                in.resetReaderIndex();
                return;
            }

            int messageType = resolveMessageType(messageHeader);
            Class<? extends Message> clazz = manager.getMessageClass(messageType);
            if (in.hasArray()) {
                int contentOffset = in.readerIndex(); // 正文起始偏移量
                out.add(serializationHandler.deserialize(clazz, in.readerIndex(contentOffset + dataLengh).array(),
                        contentOffset, dataLengh));
            } else {
                byte[] messageBody = new byte[dataLengh];
                in.readBytes(messageBody);
                out.add(serializationHandler.deserialize(clazz, messageBody));
            }
        }
    }
}