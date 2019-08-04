package conglin.clrpc.transfer.net.handler;

import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.codec.RpcDecoder;
import conglin.clrpc.transfer.codec.RpcEncoder;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class BasicClientChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ClientChannelInitializer{
    
    private BasicClientChannelHandler clientChannelHandler;

    private final ClientServiceHandler serviceHandler;

    public BasicClientChannelInitializer(ClientServiceHandler serviceHandler){
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        clientChannelHandler = new BasicClientChannelHandler(serviceHandler);

        channelPipeline.addLast(RpcEncoder.getEncoder(BasicRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(RpcDecoder.getDecoder(BasicResponse.class))
                .addLast(clientChannelHandler);
    }

    @Override
    public BasicClientChannelHandler getBasicClientChannelHandler(){
        return clientChannelHandler;
    }
}