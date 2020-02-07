package conglin.clrpc.extension.cache.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.extension.cache.CacheManager;
import conglin.clrpc.extension.cache.caffeine.CaffeineCacheManager;
import conglin.clrpc.service.context.CommonContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.CacheableResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * 检查请求对应的回复缓存是否存在，若存在且可用，则进行某些必要处理，反之将对象传递给下一个 ChannelHandler
 * 
 * 该处理器的作用是检查缓存中是否存在基本请求{@link conglin.clrpc.transport.message.BasicRequest}
 * 对应的基本回复{@link conglin.clrpc.transport.message.BasicResponse} 并进行必要处理
 */
abstract public class AbstractCacheCheckedChannelHandler<T extends BasicRequest> extends SimpleChannelInboundHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCacheCheckedChannelHandler.class);

    private final CacheManager<BasicRequest, CacheableResponse> cacheManager;

    public AbstractCacheCheckedChannelHandler(CommonContext context) {
        @SuppressWarnings("unchecked")
        CacheManager<BasicRequest, CacheableResponse> cm = (CacheManager<BasicRequest, CacheableResponse>)context.getExtensionObject().get("cacheManager");
        if(cm == null){
            cm = new CaffeineCacheManager(context.getPropertyConfigurer());
            context.getExtensionObject().put("cacheManager", cm);
        }
        this.cacheManager = cm;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        if (!enableCache()) { // 未开启缓存
            super.channelRead(ctx, msg);
            return;
        }

        CacheableResponse cachedResponse = cacheManager.get(msg);

        if (cachedResponse == null) { // 未找到缓存
            super.channelRead(ctx, msg);
            return;
        }

        LOGGER.debug("Find available cached response.");
        // 找到缓存后的处理
        cache(ctx, msg, cachedResponse);
    }

    /**
     * 是否开启缓存
     * 
     * @return
     */
    protected boolean enableCache() {
        return cacheManager != null;
    }

    /**
     * 找到cache后的工作
     * 
     * @param ctx
     * @param msg
     * @param cachedResponse
     */
    abstract protected void cache(ChannelHandlerContext ctx, T msg, CacheableResponse cachedResponse);
}