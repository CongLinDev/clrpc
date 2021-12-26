package conglin.clrpc.transport.handler.codec;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.global.GlobalPayloadManager;
import conglin.clrpc.transport.handler.codec.RpcProtocolCodec.RpcProtocolDecoder;
import conglin.clrpc.transport.handler.codec.RpcProtocolCodec.RpcProtocolEncoder;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.UnknownPayloadTypeException;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * <pre>
 *  --------------------------------------------------------------------------------------
 *  | 字节数 |   1     |         1        |        8       |        4        |      n     |
 *  |  解释 |  VERSION |   payload type   |   message id   |  payload length |   payload  |
 *  --------------------------------------------------------------------------------------
 * 
 * </pre>
 */

public class RpcProtocolCodec extends CombinedChannelDuplexHandler<RpcProtocolDecoder, RpcProtocolEncoder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcProtocolCodec.class);

    // 消息类型管理
    private static final GlobalPayloadManager manager = GlobalPayloadManager.manager();

    private static final int CURRENT_VERSION = 0;

    private final SerializationHandler serializationHandler;

    public RpcProtocolCodec(SerializationHandler serializationHandler) {
        this.serializationHandler = serializationHandler;
        init(new RpcProtocolDecoder(), new RpcProtocolEncoder());
    }

    /**
     * 编码器
     */
    class RpcProtocolEncoder extends MessageToByteEncoder<Message> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            byte[] data = serializationHandler.serialize(msg.payload());
            out.writeByte(CURRENT_VERSION);
            out.writeByte(msg.payload().payloadType());
            out.writeLong(msg.messageId());
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
            if (in.readableBytes() <= 16)
                return;
            in.markReaderIndex();

            byte version = in.readByte(); // version
            if (version != CURRENT_VERSION) {
                LOGGER.error("Unsupported version={} from {}.", version, getClass());
                return;
            }

            byte payloadType = in.readByte(); // payload type
            long messageId = in.readLong();     // messageId
            int dataLength = in.readInt();      // data length

            if (dataLength <= 0) {
                LOGGER.error("Error format message whose length is negative.");
                return;
            }

            if (in.readableBytes() < dataLength) {
                in.resetReaderIndex();
                return;
            }
            try {
                Class<? extends Payload> clazz = manager.getPayloadClass(payloadType);
                Payload payload = null;
                if (in.hasArray()) {
                    int contentOffset = in.readerIndex(); // 正文起始偏移量
                    payload = serializationHandler.deserialize(clazz, in.readerIndex(contentOffset + dataLength).array(),
                            contentOffset, dataLength);
                } else {
                    byte[] messageBody = new byte[dataLength];
                    in.readBytes(messageBody);
                    payload = serializationHandler.deserialize(clazz, messageBody);
                }
                out.add(new Message(messageId, payload));
            } catch (UnknownPayloadTypeException e) {
                LOGGER.error("unknown message id={} type={} from={}", messageId, e.getType(), ctx.channel().remoteAddress());
            }
        }
    }
}