package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.executor.AbstractProviderServiceExecutor;
import conglin.clrpc.transport.message.TransactionRequest;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TransactionRequestChannelHandler extends SimpleChannelInboundHandler<TransactionRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionRequestChannelHandler.class);

    protected final AbstractProviderServiceExecutor serviceExecutor;

    public TransactionRequestChannelHandler(AbstractProviderServiceExecutor serviceExecutor) {
        this.serviceExecutor = serviceExecutor;
    }

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        serviceExecutor.bindChannel(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransactionRequest msg) throws Exception {
        serviceExecutor.execute(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage());
        ctx.close();
    }
}