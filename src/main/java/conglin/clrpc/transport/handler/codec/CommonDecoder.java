package conglin.clrpc.transport.handler.codec;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.global.GlobalMessageManager;
import conglin.clrpc.transport.message.Message;
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

    // 消息类型管理
    private final GlobalMessageManager manager;

    public CommonDecoder(SerializationHandler serializationHandler) {
        super();
        this.serializationHandler = serializationHandler;
        manager = GlobalMessageManager.manager();
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

        int messageType = messageHeader & Message.MESSAGE_TYPE_MASK;
        Class<? extends Message> clazz = manager.getMessageClass(messageType);
        byte[] messageBody = new byte[dataLengh];
        in.readBytes(messageBody);
        out.add(serializationHandler.deserialize(messageBody, clazz));
    }
}