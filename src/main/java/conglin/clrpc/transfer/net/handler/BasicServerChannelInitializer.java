package conglin.clrpc.transfer.net.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.codec.CodecFactory;
import conglin.clrpc.transfer.codec.protostuff.ProtostuffCodecFactory;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class BasicServerChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ServerChannelInitializer{
            
    private static final Logger log = LoggerFactory.getLogger(BasicServerChannelInitializer.class);

    private static final CodecFactory codecFactory;
    static{

        String codecFactoryName = ConfigParser.getOrDefault("service.codec.factory-class",
                "conglin.clrpc.transfer.codec.protostuff.ProtostuffCodecFactory");
        CodecFactory tempFactory = null;
        try {
            Class<?> clazz = Class.forName(codecFactoryName);
            Method method = clazz.getMethod("getInstance");
            method.setAccessible(true);
            tempFactory = CodecFactory.class.cast(method.invoke(null));
        } catch (ClassNotFoundException | NoSuchMethodException | SecurityException | IllegalAccessException
                | IllegalArgumentException | InvocationTargetException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.transfer.codec.protostuff.ProtostuffCodecFactory' rather than "
                    + codecFactoryName);
		}finally{
            codecFactory = (tempFactory == null) ? ProtostuffCodecFactory.getInstance() : tempFactory;
        }
    }

    private final ServerServiceHandler serviceHandler;
    
    public BasicServerChannelInitializer(ServerServiceHandler serviceHandler){
        super();
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
        .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
        .addLast(codecFactory.getDecoder(BasicRequest.class))
        .addLast(codecFactory.getEncoder(BasicResponse.class))
        .addLast(new BasicServerChannelHandler(serviceHandler));
        // you can add more handlers
    }
}