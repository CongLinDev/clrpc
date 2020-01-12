package conglin.clrpc.extension.cache.handler;

import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务消费者端的缓存处理器实现
 */

public class ConsumerCacheCheckedChannelHandler<T extends BasicRequest> extends AbstractCacheCheckedChannelHandler<T> {

    private final FuturesHolder<Long> futuresHolder;

    public ConsumerCacheCheckedChannelHandler(ConsumerContext context) {
        super(context);
        if (enableCache()) {
            this.futuresHolder = context.getFuturesHolder();
        } else {
            this.futuresHolder = null;
        }
    }

    @Override
    protected void cache(ChannelHandlerContext ctx, T msg, BasicResponse cachedResponse) {
        futuresHolder.getFuture(msg.getRequestId()).done(cachedResponse);
    }
}