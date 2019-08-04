package conglin.clrpc.transfer.net.handler;

import conglin.clrpc.transfer.codec.RpcDecoder;
import conglin.clrpc.transfer.codec.RpcEncoder;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;
import conglin.clrpc.transfer.net.receiver.RequestReceiver;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class BasicServerChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ServerChannelInitializer{
    private final RequestReceiver requestReceiver;
    
    public BasicServerChannelInitializer(RequestReceiver requestReceiver){
        super();
        this.requestReceiver = requestReceiver;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
        .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
        .addLast(RpcDecoder.getDecoder(BasicRequest.class))
        .addLast(RpcEncoder.getEncoder(BasicResponse.class))
        .addLast(new BasicServerChannelHandler(requestReceiver));
        // you can add more handlers
    }
}