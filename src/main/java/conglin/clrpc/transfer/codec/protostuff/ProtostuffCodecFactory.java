package conglin.clrpc.transfer.codec.protostuff;

import conglin.clrpc.transfer.codec.CodecFactory;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProtostuffCodecFactory implements CodecFactory {

    private ProtostuffCodecFactory(){
        
    }

    public static CodecFactory getInstance() {
        return SingletonHolder.CODEC_FACTORY;
    }

    private static class SingletonHolder {
        private static final ProtostuffCodecFactory CODEC_FACTORY = new ProtostuffCodecFactory();
    }

    @Override
    public MessageToByteEncoder<?> getEncoder(Class<?> genericClass) {
        return new ProtostuffEncoder(genericClass);
    }

    @Override
    public ByteToMessageDecoder getDecoder(Class<?> genericClass) {
		return new ProtostuffDecoder(genericClass);
	}

}