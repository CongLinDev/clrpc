package conglin.clrpc.transfer.handler;

import conglin.clrpc.transfer.handler.codec.RpcDecoder;
import conglin.clrpc.transfer.handler.codec.RpcEncoder;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.receiver.ResponseReceiver;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class BasicConsumerChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ConsumerChannelInitializer{
    
    private BasicConsumerChannelHandler consumerChannelHandler;

    private final ResponseReceiver receiver;

    public BasicConsumerChannelInitializer(ResponseReceiver receiver){
        this.receiver = receiver;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        consumerChannelHandler = new BasicConsumerChannelHandler(receiver);

        channelPipeline.addLast(RpcEncoder.getEncoder(BasicRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(RpcDecoder.getDecoder(BasicResponse.class))
                .addLast(consumerChannelHandler);
    }

    @Override
    public BasicConsumerChannelHandler getBasicConsumerChannelHandler(){
        return consumerChannelHandler;
    }
}