package conglin.clrpc.extension.cache.handler;

import conglin.clrpc.service.context.channel.ConsumerChannelContext;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicRequest;

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
public class ConsumerCacheCheckedChannelHandler<T extends BasicRequest> extends AbstractCacheChannelHandler<T> {

    private final FutureHolder<Long> futureHolder;

    public ConsumerCacheCheckedChannelHandler(ConsumerChannelContext context) {
        super(context);
        this.futureHolder = context.futureHolder();
    }

    @Override
    protected boolean check(T msg) {
        return cacheManager().isExist(msg);
    }

    @Override
    protected Object cache(T msg) {
        Long messageId = msg.messageId();
        RpcFuture future = futureHolder.removeFuture(messageId);

        if (future != null) {
            future.done(cacheManager().get(msg).copy(messageId));
        }
        return null;
    }
}