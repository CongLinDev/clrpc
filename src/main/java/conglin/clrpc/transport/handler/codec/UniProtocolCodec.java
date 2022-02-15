package conglin.clrpc.transport.handler.codec;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.transport.handler.codec.UniProtocolCodec.RpcProtocolDecoder;
import conglin.clrpc.transport.handler.codec.UniProtocolCodec.RpcProtocolEncoder;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.protocol.ProtocolDefinition;
import conglin.clrpc.transport.protocol.UnknownPayloadTypeException;
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

public class UniProtocolCodec extends CombinedChannelDuplexHandler<RpcProtocolDecoder, RpcProtocolEncoder> implements ComponentContextAware, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(UniProtocolCodec.class);

    private ComponentContext context;

    private SerializationHandler serializationHandler;

    private ProtocolDefinition protocolDefinition;


    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    public void init() {
        this.serializationHandler = getContext().getWith(ComponentContextEnum.SERIALIZATION_HANDLER);
        this.protocolDefinition = getContext().getWith(ComponentContextEnum.PROTOCOL_DEFINITION);
        init(new RpcProtocolDecoder(), new RpcProtocolEncoder());
    }

    /**
     * 编码器
     */
    class RpcProtocolEncoder extends MessageToByteEncoder<Message> {
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
    class RpcProtocolDecoder extends ByteToMessageDecoder {

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