package conglin.clrpc.transport.handler;

import conglin.clrpc.common.object.Pair;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.ResponsePayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderResponseChannelHandler
        extends SimpleChannelInboundHandler<Pair<? extends Message, ? extends ResponsePayload>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderResponseChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Pair<? extends Message, ? extends ResponsePayload> msg)
            throws Exception {
        ResponsePayload response = msg.getSecond();
        ctx.writeAndFlush(new Message(msg.getFirst().messageId(), response));
        LOGGER.debug("Send response which messageId={}", msg.getFirst().messageId());
    }
}