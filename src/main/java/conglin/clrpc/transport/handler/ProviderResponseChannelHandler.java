package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ProviderResponseChannelHandler
        extends SimpleChannelInboundHandler<Pair<? extends BasicRequest, ? extends BasicResponse>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderResponseChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Pair<? extends BasicRequest, ? extends BasicResponse> msg)
            throws Exception {
        BasicResponse response = msg.getSecond();
        ctx.writeAndFlush(response);
        LOGGER.debug("Send response which messageId={}", response.getMessageId());
    }
}