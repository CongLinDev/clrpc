package conglin.clrpc.transfer.handler;

import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.transfer.handler.codec.BasicRequestEncoder;
import conglin.clrpc.transfer.handler.codec.CommonDecoder;
import conglin.clrpc.transfer.handler.codec.TransactionRequestEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel>{
    
    private ChannelPipeline channelPipeline;

    protected final ConsumerContext context;

    public ConsumerChannelInitializer(ConsumerContext context){
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        this.channelPipeline = ch.pipeline();
        this.channelPipeline
                .addLast("BasicRequest Encoder", new BasicRequestEncoder(context.getSerializationHandler()))
                .addLast("TransactionRequest Encoder", new TransactionRequestEncoder(context.getSerializationHandler()))
                .addLast("Common Decoder", new CommonDecoder(context.getSerializationHandler()))
                .addLast("BasicResponse ChannelHandler", 
                    new BasicResponseChannelHandler(context.getServiceExecutor()));
        // you can add more handlers
    }

    /**
     * 返回当前绑定的 {@link io.netty.channel.Channel}
     * @return
     */
    public Channel channel(){
        return channelPipeline.channel();
    }
}