package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.transport.message.BasicRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class ConsumerRequestChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRequestChannelHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof BasicRequest) {
            BasicRequest request = (BasicRequest) msg;
            ctx.writeAndFlush(request);
            LOGGER.debug("Send request for messageId={}", request.getMessageId());
        } else {
            super.channelRead(ctx, msg);
        }
    }
}