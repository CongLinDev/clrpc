package conglin.clrpc.transfer.codec;

import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 实现该接口的同时应当实现
 * public static CodecFactory getInstance(); 静态方法
 * 用于获取工厂实例
 */
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