package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.executor.ServiceExecutor;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConsumerChannelInboundHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerChannelInboundHandler.class);

    private final ServiceExecutor<BasicResponse> serviceExecutor;

    public ConsumerChannelInboundHandler(ServiceExecutor<BasicResponse> serviceExecutor) {
        this.serviceExecutor = serviceExecutor;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        serviceExecutor.execute((BasicResponse) msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error(cause.getMessage());
        ctx.close();
    }
}