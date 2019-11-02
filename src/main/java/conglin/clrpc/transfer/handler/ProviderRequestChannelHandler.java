package conglin.clrpc.transfer.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.executor.AbstractProviderServiceExecutor;
import conglin.clrpc.transfer.message.BasicRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProviderRequestChannelHandler<T extends BasicRequest> extends SimpleChannelInboundHandler<T> {
    private static final Logger log = LoggerFactory.getLogger(ProviderRequestChannelHandler.class);

    protected final AbstractProviderServiceExecutor serviceExecutor;

    public ProviderRequestChannelHandler(AbstractProviderServiceExecutor serviceExecutor){
        this.serviceExecutor = serviceExecutor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        serviceExecutor.bindChannel(ctx.channel());
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        super.channelUnregistered(ctx);
        if(!serviceExecutor.isDestroyed())
            serviceExecutor.destroy();
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        serviceExecutor.execute(msg);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.close();
        if(!serviceExecutor.isDestroyed())
            serviceExecutor.destroy();
    }

}