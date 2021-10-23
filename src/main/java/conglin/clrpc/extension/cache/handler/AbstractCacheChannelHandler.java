package conglin.clrpc.extension.cache.handler;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.extension.cache.CacheManager;
import conglin.clrpc.extension.cache.CacheableResponse;
import conglin.clrpc.global.GlobalMessageManager;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.transport.message.BasicRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

abstract public class AbstractCacheChannelHandler<T> extends SimpleChannelInboundHandler<T> implements ContextAware, Initializable {

    private RpcContext context;

    private CacheManager<BasicRequest, CacheableResponse> cacheManager;

    @Override
    public RpcContext getContext() {
        return context;
    }

    @Override
    public void setContext(RpcContext context) {
        this.context = context;
    }

    @Override
    public void init() {
        GlobalMessageManager.manager().setMessageClass(CacheableResponse.MESSAGE_TYPE, CacheableResponse.class);
        PropertyConfigurer configurer = getContext().getWith(RpcContextEnum.PROPERTY_CONFIGURER);
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