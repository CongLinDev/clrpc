package conglin.clrpc.transfer.handler;

import conglin.clrpc.transfer.handler.codec.BasicResponseEncoder;
import conglin.clrpc.transfer.handler.codec.CommonDecoder;
import conglin.clrpc.transfer.receiver.RequestReceiver;
import conglin.clrpc.transfer.sender.ResponseSender;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel>{
    
    private final ResponseSender sender;
    private final RequestReceiver receiver;
    
    public ProviderChannelInitializer(ResponseSender sender, RequestReceiver receiver){
        super();
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
            .addLast("Common Decoder", new CommonDecoder())
            .addLast("BasicResponse Encoder", new BasicResponseEncoder())
            .addLast("Provider ChannelHandler", new BasicRequestChannelHandler(sender, receiver));
        // you can add more handlers
    }
}