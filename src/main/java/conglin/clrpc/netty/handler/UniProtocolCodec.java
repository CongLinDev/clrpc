package conglin.clrpc.netty.handler;

import conglin.clrpc.invocation.message.Message;
import conglin.clrpc.invocation.message.Payload;
import conglin.clrpc.invocation.protocol.ProtocolDefinition;
import conglin.clrpc.invocation.protocol.UnknownPayloadTypeException;
import conglin.clrpc.invocation.serialization.SerializationHandler;
import conglin.clrpc.netty.handler.UniProtocolCodec.ProtocolDecoder;
import conglin.clrpc.netty.handler.UniProtocolCodec.ProtocolEncoder;
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

public class UniProtocolCodec extends CombinedChannelDuplexHandler<ProtocolDecoder, ProtocolEncoder> {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniProtocolCodec.class);

    private final SerializationHandler serializationHandler;

    private final ProtocolDefinition protocolDefinition;

    public UniProtocolCodec(SerializationHandler serializationHandler, ProtocolDefinition protocolDefinition) {
        this.serializationHandler = serializationHandler;
        this.protocolDefinition = protocolDefinition;
        init(new ProtocolDecoder(), new ProtocolEncoder());
    }

    /**
     * 编码器
     */
    class ProtocolEncoder extends MessageToByteEncoder<Message> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Message msg, ByteBuf out) throws Exception {
            byte[] data = serializationHandler.serialize(msg.payload());
            out.writeByte(protocolDefinition.version());
            out.writeByte(protocolDefinition.getTypeByPayload(msg.payload().getClass()));
            out.writeLong(msg.messageId());
            out.writeInt(data.length);
            out.writeBytes(data);
        }
    }

    /**
     * 解码器
     */
    class ProtocolDecoder extends ByteToMessageDecoder {

        @Override
        protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            if (in.readableBytes() <= 16)
                return;
            in.markReaderIndex();

            byte version = in.readByte(); // version
            if (version != protocolDefinition.version()) {
                LOGGER.warn("Unsupported version={} from {}.", version, getClass());
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
                Class<? extends Payload> clazz = protocolDefinition.getPayloadByType(payloadType);
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