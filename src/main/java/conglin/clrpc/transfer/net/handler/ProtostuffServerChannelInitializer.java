package conglin.clrpc.transfer.net.handler;

import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.codec.protostuff.ProtostuffDecoder;
import conglin.clrpc.transfer.codec.protostuff.ProtostuffEncoder;
import conglin.clrpc.transfer.net.BasicRequest;
import conglin.clrpc.transfer.net.BasicResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtostuffServerChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ServerChannelInitializer{

    private ServerServiceHandler serviceHandler;
    
    public ProtostuffServerChannelInitializer(ServerServiceHandler serviceHandler){
        super();
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
        .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
        .addLast(new ProtostuffDecoder(BasicRequest.class))
        .addLast(new ProtostuffEncoder(BasicResponse.class))
        .addLast(new BasicServerChannelHandler(serviceHandler));
        // you can add more handlers
    }
}