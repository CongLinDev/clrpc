package conglin.clrpc.transport.handler;

import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.RequestPayload;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerRequestChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRequestChannelHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Message message && message.payload() instanceof RequestPayload) {
            ctx.writeAndFlush(message);
            LOGGER.debug("Send request for messageId={}", message.messageId());
        } else {
            super.channelRead(ctx, msg);
        }
    }
}