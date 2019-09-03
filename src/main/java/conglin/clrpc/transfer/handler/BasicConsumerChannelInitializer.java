package conglin.clrpc.transfer.handler;

import conglin.clrpc.transfer.handler.codec.RpcDecoder;
import conglin.clrpc.transfer.handler.codec.RpcEncoder;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.receiver.ResponseReceiver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class BasicConsumerChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ConsumerChannelInitializer{
    
    private ChannelPipeline channelPipeline;

    private final ResponseReceiver receiver;

    public BasicConsumerChannelInitializer(ResponseReceiver receiver){
        this.receiver = receiver;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        this.channelPipeline = ch.pipeline();
        this.channelPipeline
                .addLast(RpcEncoder.getEncoder(BasicRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(RpcDecoder.getDecoder(BasicResponse.class))
                .addLast(new BasicConsumerChannelHandler(receiver));
    }


    @Override
    public Channel channel(){
        return channelPipeline.channel();
    }
}