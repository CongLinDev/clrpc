package conglin.clrpc.transport.handler.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务提供者端的缓存处理器实现
 */

public class ProviderCacheCheckedChannelHandler extends AbstractCacheCheckedChannelHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderCacheCheckedChannelHandler.class);

    public ProviderCacheCheckedChannelHandler(ProviderContext context) {
        super(context);
    }

    @Override
    protected void cache(ChannelHandlerContext ctx, Long requestId, BasicResponse cachedResponse) {
        // response.setRequestId(requestId);
        BasicResponse response = new BasicResponse(requestId);
        response.setResult(cachedResponse.getResult());
        send(ctx, response);
    }

    /**
     * 发送回复
     * @param ctx
     * @param response
     */
    protected void send(ChannelHandlerContext ctx, BasicResponse response) {
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        LOGGER.debug("Sending response for request id=" + response.getRequestId());
    }
}