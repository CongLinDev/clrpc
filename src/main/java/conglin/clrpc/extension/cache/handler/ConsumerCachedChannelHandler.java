package conglin.clrpc.extension.cache.handler;

import conglin.clrpc.common.Pair;
import conglin.clrpc.service.context.channel.ConsumerChannelContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.CacheableResponse;

/**
 * 将接收到的请求与回复进行缓存，反之将对象传递给下一个 ChannelHandler
 * 
 * 该处理器的作用时机是处理请求的业务逻辑后
 * 
 * 该处理器的作用将有效的{@link conglin.clrpc.transport.message.BasicRequest} 与
 * 对应的可缓存的基本回复{@link conglin.clrpc.transport.message.CacheableResponse} 加入缓存
 * 
 * @param <T>
 */
public class ConsumerCachedChannelHandler<T extends Pair<? extends BasicRequest, ? extends CacheableResponse>>
        extends AbstractCacheChannelHandler<T> {

    public ConsumerCachedChannelHandler(ConsumerChannelContext context) {
        super(context);
    }

    @Override
    protected boolean check(T msg) {
        BasicResponse response = msg.getSecond();
        return response != null && !response.isError();
    }

    @Override
    protected Object cache(T msg) {
        cacheManager().put(msg.getFirst(), msg.getSecond());
        return null;
    }
}