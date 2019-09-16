package conglin.clrpc.transfer.handler;

import conglin.clrpc.transfer.handler.codec.BasicRequestEncoder;
import conglin.clrpc.transfer.handler.codec.CommonDecoder;
import conglin.clrpc.transfer.handler.codec.TransactionRequestEncoder;
import conglin.clrpc.transfer.receiver.ResponseReceiver;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ConsumerChannelInitializer 
        extends ChannelInitializer<SocketChannel>{
    
    private ChannelPipeline channelPipeline;

    private final ResponseReceiver receiver;

    public ConsumerChannelInitializer(ResponseReceiver receiver){
        this.receiver = receiver;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        this.channelPipeline = ch.pipeline();
        this.channelPipeline
                .addLast(new BasicRequestEncoder())
                .addLast(new TransactionRequestEncoder())
                .addLast(new CommonDecoder())
                .addLast(new BasicConsumerChannelHandler(receiver));
        // you can add more handlers
    }

    public Channel channel(){
        return channelPipeline.channel();
    }
}