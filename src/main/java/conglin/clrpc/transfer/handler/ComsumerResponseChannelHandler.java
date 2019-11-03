package conglin.clrpc.transfer.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.executor.ServiceExecutor;
import conglin.clrpc.transfer.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ComsumerResponseChannelHandler
        extends SimpleChannelInboundHandler<BasicResponse>{

    private static final Logger log = LoggerFactory.getLogger(ComsumerResponseChannelHandler.class);

    private final ServiceExecutor<BasicResponse> serviceExecutor;

    public ComsumerResponseChannelHandler(ServiceExecutor<BasicResponse> serviceExecutor){
        this.serviceExecutor = serviceExecutor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicResponse msg) throws Exception {
        serviceExecutor.execute(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.close();
        if(!serviceExecutor.isDestroyed())
            serviceExecutor.destroy();
    }

    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        if(!serviceExecutor.isDestroyed())
            serviceExecutor.destroy();
    }
}