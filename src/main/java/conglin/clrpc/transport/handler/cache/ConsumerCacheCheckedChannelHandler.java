package conglin.clrpc.transport.handler.cache;

import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;

/**
 * 服务消费者端的缓存处理器实现
 */

public class ConsumerCacheCheckedChannelHandler extends AbstractCacheCheckedChannelHandler {

    private final FuturesHolder<Long> FUTURES_HOLDER;

    public ConsumerCacheCheckedChannelHandler(ConsumerContext context) {
        super(context);
        if (enableCache()) {
            this.FUTURES_HOLDER = context.getFuturesHolder();
        } else {
            this.FUTURES_HOLDER = null;
        }
    }

    @Override
    protected void cache(ChannelHandlerContext ctx, Long requestId, BasicResponse response) {
        FUTURES_HOLDER.getFuture(requestId).done(response);
    }

}