package conglin.clrpc.transfer.net.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.message.BasicResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BasicClientChannelHandler 
        extends SimpleChannelInboundHandler<BasicResponse> {

    private static final Logger log = LoggerFactory.getLogger(BasicClientChannelHandler.class);

    private ClientServiceHandler serviceHandler;

    private Channel channel;

    public BasicClientChannelHandler(ClientServiceHandler serviceHandler){
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicResponse msg) throws Exception {
        String requestId = msg.getRequestId();
        
        //直接移除
        RpcFuture future = serviceHandler.removeFuture(requestId);

        if(future != null){
            future.done(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.close();
    }

    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    public Channel getChannel(){
        return channel;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
    

}