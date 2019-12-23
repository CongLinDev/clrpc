package conglin.clrpc.transport.handler;

import conglin.clrpc.common.codec.SerializationHandler;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.transport.handler.codec.BasicRequestEncoder;
import conglin.clrpc.transport.handler.codec.CommonDecoder;
import conglin.clrpc.transport.handler.codec.TransactionRequestEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelPipeline channelPipeline;

    protected final ConsumerContext context;

    public ConsumerChannelInitializer(ConsumerContext context) {
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        this.channelPipeline = ch.pipeline();
        SerializationHandler serializationHandler = context.getSerializationHandler();
        this.channelPipeline.addLast("BasicRequest Encoder", new BasicRequestEncoder(serializationHandler))
                .addLast("TransactionRequest Encoder", new TransactionRequestEncoder(serializationHandler))
                .addLast("Common Decoder", new CommonDecoder(serializationHandler))
                .addLast("ConsumerChannelInboundHandler",
                        new ConsumerChannelInboundHandler(context.getServiceExecutor()));
        // you can add more handlers
    }

    /**
     * 返回当前绑定的 {@link io.netty.channel.Channel}
     * 
     * @return
     */
    public Channel channel() {
        return channelPipeline.channel();
    }
}