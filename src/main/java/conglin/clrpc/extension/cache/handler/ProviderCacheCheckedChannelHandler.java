package conglin.clrpc.extension.cache.handler;

import conglin.clrpc.common.object.Pair;
import conglin.clrpc.service.context.channel.ProviderChannelContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

/**
 * 检查请求对应的回复缓存是否存在，若存在且可用，则进行某些必要处理，反之将对象传递给下一个 ChannelHandler
 * 
 * 该处理器的作用时机是处理请求的业务逻辑前
 * 
 * 该处理器的作用是检查缓存中是否存在基本请求{@link conglin.clrpc.transport.message.BasicRequest}
 * 对应的基本回复{@link conglin.clrpc.transport.message.BasicResponse} 并进行必要处理
 * 
 * @param <T>
 */
public class ProviderCacheCheckedChannelHandler<T extends BasicRequest> extends AbstractCacheChannelHandler<T> {

    public ProviderCacheCheckedChannelHandler(ProviderChannelContext context) {
        super(context);
    }

    @Override
    protected boolean check(T msg) {
        return cacheManager().isExist(msg);
    }

    @Override
    protected Object cache(T msg) {
        Long messageId = msg.messageId();
        return new Pair<BasicRequest, BasicResponse>(msg, cacheManager().get(msg).copy(messageId));
    }
}