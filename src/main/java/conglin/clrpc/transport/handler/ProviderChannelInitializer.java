package conglin.clrpc.transport.handler;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.service.executor.ZooKeeperProviderServiceExecutor;
import conglin.clrpc.transport.handler.codec.BasicResponseEncoder;
import conglin.clrpc.transport.handler.codec.CommonDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ProviderContext context;

    public ProviderChannelInitializer(ProviderContext context) {
        super();
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        SerializationHandler serializationHandler = context.getSerializationHandler();
        pipeline.addLast("Common Decoder", new CommonDecoder(serializationHandler))
                .addLast("BasicResponse Encoder", new BasicResponseEncoder(serializationHandler))
                .addLast("ProviderChannelInboundHandler",
                        new ProviderChannelInboundHandler(new ZooKeeperProviderServiceExecutor(context)));

        // you can add more handlers
    }

}