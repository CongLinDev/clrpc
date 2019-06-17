package conglin.clrpc.transfer.net.handler;

import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.codec.protostuff.ProtostuffDecoder;
import conglin.clrpc.transfer.codec.protostuff.ProtostuffEncoder;
import conglin.clrpc.transfer.net.BasicRequest;
import conglin.clrpc.transfer.net.BasicResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

public class ProtostuffClientChannelInitializer 
        extends ChannelInitializer<SocketChannel>
        implements ClientChannelInitializer{

    private BasicClientChannelHandler clientChannelHandler;

    private ClientServiceHandler serviceHandler;

    public ProtostuffClientChannelInitializer(ClientServiceHandler serviceHandler){
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline channelPipeline = ch.pipeline();

        clientChannelHandler = new BasicClientChannelHandler(serviceHandler);

        channelPipeline.addLast(new ProtostuffEncoder(BasicRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(65536, 0, 4, 0, 0))
                .addLast(new ProtostuffDecoder(BasicResponse.class))
                .addLast(clientChannelHandler);
    }

    @Override
    public BasicClientChannelHandler getBasicClientChannelHandler(){
        return clientChannelHandler;
    }
}