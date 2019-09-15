package conglin.clrpc.transfer.handler;

import conglin.clrpc.transfer.handler.codec.CommonDecoder;
import conglin.clrpc.transfer.handler.codec.RpcDecoder;
import conglin.clrpc.transfer.handler.codec.RpcEncoder;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.message.TransactionRequest;
import conglin.clrpc.transfer.receiver.RequestReceiver;
import conglin.clrpc.transfer.sender.ResponseSender;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProviderChannelInitializer 
        extends ChannelInitializer<SocketChannel>{
    
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
            // .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 8, 0, 0))
            .addLast(RpcDecoder.getDecoder(BasicRequest.class))
            // .addLast(RpcDecoder.getDecoder(TransactionRequest.class))
            // .addLast(new CommonDecoder()) // replace above handlers
            .addLast(RpcEncoder.getEncoder(BasicResponse.class))
            .addLast(new BasicProviderChannelHandler(sender, receiver));
        // you can add more handlers
    }
}