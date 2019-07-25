package conglin.clrpc.transfer.codec;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public interface CodecFactory {
    /**
     * 得到一个编码器
     * @return
     */
    MessageToByteEncoder<?> getEncoder(Class<?> genericClass);

    /**
     * 得到一个解码器
     * @return
     */
    ByteToMessageDecoder getDecoder(Class<?> genericClass);
}