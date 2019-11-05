package conglin.clrpc.transfer.handler;

import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.service.executor.BasicProviderServiceExecutor;
import conglin.clrpc.service.executor.ZooKeeperProviderServiceExecutor;
import conglin.clrpc.transfer.handler.codec.BasicResponseEncoder;
import conglin.clrpc.transfer.handler.codec.CommonDecoder;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel>{
    
    private final ProviderContext context;
    
    public ProviderChannelInitializer(ProviderContext context){
        super();
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
            .addLast("Common Decoder", new CommonDecoder(context.getSerializationHandler()))
            .addLast("BasicResponse Encoder", new BasicResponseEncoder(context.getSerializationHandler()))
            .addLast("Provider BasicRequest-ChannelHandler", 
                new ProviderRequestChannelHandler(new BasicProviderServiceExecutor(context)))
            .addLast("Provider TransactionRequest-ChannelHandler", 
                new ProviderRequestChannelHandler(new ZooKeeperProviderServiceExecutor(context)));
            
        // you can add more handlers
    }


}