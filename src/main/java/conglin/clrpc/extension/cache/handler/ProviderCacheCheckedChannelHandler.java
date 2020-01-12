package conglin.clrpc.extension.cache.handler;

import conglin.clrpc.common.Pair;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务提供者端的缓存处理器实现
 */

public class ProviderCacheCheckedChannelHandler<T extends BasicRequest> extends AbstractCacheCheckedChannelHandler<T> {

    public ProviderCacheCheckedChannelHandler(ProviderContext context) {
        super(context);
    }

    @Override
    protected void cache(ChannelHandlerContext ctx, T msg, BasicResponse cachedResponse) {
        BasicResponse response = new BasicResponse(msg.getRequestId());
        response.setResult(cachedResponse.getResult());

        ctx.fireChannelRead(new Pair<T, BasicResponse>(msg, response));
    }
}