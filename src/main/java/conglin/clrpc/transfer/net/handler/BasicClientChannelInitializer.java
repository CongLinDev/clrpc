package conglin.clrpc.transfer.net.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.codec.CodecFactory;
import conglin.clrpc.transfer.codec.protostuff.ProtostuffCodecFactory;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class BasicClientChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ClientChannelInitializer{
    private static final Logger log = LoggerFactory.getLogger(BasicClientChannelInitializer.class);

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

    
    private BasicClientChannelHandler clientChannelHandler;

    private final ClientServiceHandler serviceHandler;

    public BasicClientChannelInitializer(ClientServiceHandler serviceHandler){
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        clientChannelHandler = new BasicClientChannelHandler(serviceHandler);

        channelPipeline.addLast(codecFactory.getEncoder(BasicRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(codecFactory.getDecoder(BasicResponse.class))
                .addLast(clientChannelHandler);
    }

    @Override
    public BasicClientChannelHandler getBasicClientChannelHandler(){
        return clientChannelHandler;
    }
}