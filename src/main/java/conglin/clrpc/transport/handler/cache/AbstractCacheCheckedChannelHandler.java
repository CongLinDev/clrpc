package conglin.clrpc.transport.handler.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.context.CommonContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * 检查请求对应的回复缓存是否存在，若存在且可用，则进行某些必要处理，反之将对象传递给下一个 ChannelHandler
 * 
 * 该处理器的作用是检查缓存中是否存在基本请求{@link conglin.clrpc.transport.message.BasicRequest}
 * 对应的基本回复{@link conglin.clrpc.transport.message.BasicResponse} 并进行必要处理
 */
abstract public class AbstractCacheCheckedChannelHandler extends ChannelInboundHandlerAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCacheCheckedChannelHandler.class);

    private final CacheManager<BasicRequest, BasicResponse> CACHE_MANAGER;

    public AbstractCacheCheckedChannelHandler(CommonContext context) {
        this.CACHE_MANAGER = context.getCacheManager();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (!enableCache()) { // 未开启缓存
            super.channelRead(ctx, msg);
            return;
        }

        BasicRequest request = (BasicRequest) msg;
        BasicResponse response = CACHE_MANAGER.get(request);

        if (response == null) { // 未找到缓存
            super.channelRead(ctx, msg);
            return;
        }

        LOGGER.debug("Find available cached response.");
        // 找到缓存后的处理
        cache(ctx, request.getRequestId(), response);
    }

    /**
     * 是否开启缓存
     * 
     * @return
     */
    protected boolean enableCache() {
        return CACHE_MANAGER != null;
    }

    /**
     * 找到cache后的工作
     * 
     * @param ctx
     * @param requestId
     * @param response
     */
    abstract protected void cache(ChannelHandlerContext ctx, Long requestId, BasicResponse response);
}