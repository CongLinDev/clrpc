package conglin.clrpc.extension.cache.handler;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.extension.cache.CacheManager;
import conglin.clrpc.service.context.channel.CommonChannelContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.CacheableResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

abstract public class AbstractCacheChannelHandler<T> extends SimpleChannelInboundHandler<T> {

    private final CacheManager<BasicRequest, CacheableResponse> cacheManager;

    public AbstractCacheChannelHandler(CommonChannelContext context) {
        PropertyConfigurer configurer = context.propertyConfigurer();
        @SuppressWarnings("unchecked")
        CacheManager<BasicRequest, CacheableResponse> cm = (CacheManager<BasicRequest, CacheableResponse>) configurer
                .get("extension.cache.cacheManager");
        this.cacheManager = cm;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        Object result = null;
        if (enableCache() && check(msg) && (result = cache(msg)) != null) { // 开启缓存且存在缓存
            ctx.fireChannelRead(result);
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 检查缓存
     * 
     * @param msg
     * @return
     */
    abstract protected boolean check(T msg);

    /**
     * 找到cache后的工作
     * 
     * @param msg
     * @return result
     */
    abstract protected Object cache(T msg);

    /**
     * 是否开启缓存
     * 
     * @return
     */
    protected boolean enableCache() {
        return cacheManager != null;
    }

    /**
     * 返回缓存管理
     * 
     * @return
     */
    protected CacheManager<BasicRequest, CacheableResponse> cacheManager() {
        return cacheManager;
    }
}