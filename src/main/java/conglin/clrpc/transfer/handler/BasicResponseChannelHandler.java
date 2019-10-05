package conglin.clrpc.transfer.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.receiver.ResponseReceiver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BasicResponseChannelHandler 
        extends SimpleChannelInboundHandler<BasicResponse>{

    private static final Logger log = LoggerFactory.getLogger(BasicResponseChannelHandler.class);

    private final ResponseReceiver receiver;

    public BasicResponseChannelHandler(ResponseReceiver receiver){
        this.receiver = receiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicResponse msg) throws Exception {
        receiver.handleResponse(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }
}