package conglin.clrpc.transport.handler;

import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.ResponsePayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProviderResponseChannelHandler
        extends SimpleChannelInboundHandler<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderResponseChannelHandler.class);

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg)
            throws Exception {
        if (msg.payload() instanceof ResponsePayload) {
            ctx.writeAndFlush(msg);
            LOGGER.debug("Send response which messageId={}", msg.messageId());
        } else {
            ctx.fireChannelRead(msg);
        }
    }
}