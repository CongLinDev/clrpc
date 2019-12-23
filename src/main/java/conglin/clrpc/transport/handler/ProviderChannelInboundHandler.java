package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.executor.AbstractProviderServiceExecutor;
import conglin.clrpc.transport.message.BasicRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ProviderChannelInboundHandler extends ChannelInboundHandlerAdapter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderChannelInboundHandler.class);

    protected final AbstractProviderServiceExecutor serviceExecutor;

    public ProviderChannelInboundHandler(AbstractProviderServiceExecutor serviceExecutor) {
        this.serviceExecutor = serviceExecutor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        serviceExecutor.bindChannel(ctx.channel());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        serviceExecutor.execute((BasicRequest) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage());
        ctx.close();
    }
}