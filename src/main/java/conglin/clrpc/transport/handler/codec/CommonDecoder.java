package conglin.clrpc.transport.handler.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.TransactionRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class CommonDecoder extends ByteToMessageDecoder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommonDecoder.class);

    // ---------------------------------------------------------
    // 字节数 | 4 | 4 | n |
    // 解释 | 消息头 | 正文长度 | 正文 |
    // ---------------------------------------------------------
    // private static final int MESSAGE_HEADER_LENGTH = 4;
    // private static final int MESSAGE_BODY_LENGTH_FIELD_LENGTH = 4;

    // 配置编码器
    private final SerializationHandler serializationHandler;

    public CommonDecoder(SerializationHandler serializationHandler) {
        super();
        this.serializationHandler = serializationHandler;
    }

    /**
     * 解码
     * 
     * @param messageHeader
     * @param messageBody
     * @return
     */
    protected Object decode(int messageHeader, byte[] messageBody) {
        Object result = null;

        /**
         * 根据不同的消息类型，对消息进行解码
         * 
         * 消息类型占用一个字节
         */
        int messageType = messageHeader & Message.MESSAGE_TYPE_MASK;
        switch (messageType) {
        case BasicRequest.MESSAGE_TYPE:
            result = serializationHandler.deserialize(messageBody, BasicRequest.class);
            break;
        case BasicResponse.MESSAGE_TYPE:
            result = serializationHandler.deserialize(messageBody, BasicResponse.class);
            break;
        case TransactionRequest.MESSAGE_TYPE:
            result = serializationHandler.deserialize(messageBody, TransactionRequest.class);
            break;
        default:
            LOGGER.error("Can not decode message type=" + messageType);
        }
        return result;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (in.readableBytes() <= 8)
            return;
        in.markReaderIndex();

        int messageHeader = in.readInt();
        int dataLengh = in.readInt();

        if (dataLengh <= 0) {
            LOGGER.error("Error format message whose length is negative.");
            return;
        }

        if (in.readableBytes() < dataLengh) {
            in.resetReaderIndex();
            return;
        }

        byte[] messageBody = new byte[dataLengh];
        in.readBytes(messageBody);

        out.add(decode(messageHeader, messageBody));
    }
}