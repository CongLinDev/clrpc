package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.executor.AbstractProviderServiceExecutor;
import conglin.clrpc.transport.message.BasicRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BasicRequestChannelHandler extends SimpleChannelInboundHandler<BasicRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(BasicRequestChannelHandler.class);

    protected final AbstractProviderServiceExecutor serviceExecutor;

    public BasicRequestChannelHandler(AbstractProviderServiceExecutor serviceExecutor) {
        this.serviceExecutor = serviceExecutor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        serviceExecutor.bindChannel(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicRequest msg) throws Exception {
        serviceExecutor.execute(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage());
        ctx.close();
    }

}