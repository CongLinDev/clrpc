package conglin.clrpc.extension.cache.handler;

import java.lang.reflect.Method;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.extension.annotation.CacheableService;
import conglin.clrpc.extension.annotation.IdempotentService;
import conglin.clrpc.service.context.channel.ProviderChannelContext;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.CacheableResponse;

public class ProviderCachedChannelHandler<T extends Pair<? extends BasicRequest, ? extends BasicResponse>>
        extends AbstractCacheChannelHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderCachedChannelHandler.class);

    private final Function<String, Object> objectHolder;

    public ProviderCachedChannelHandler(ProviderChannelContext context) {
        super(context);
        this.objectHolder = context.objectHolder();
    }

    @Override
    protected boolean check(T msg) {
        BasicResponse response = msg.getSecond();
        return response != null && !response.isError();
    }

    @Override
    protected Object cache(T msg) {
        BasicRequest request = msg.getFirst();
        // 一定能找到服务对象
        Object serviceBean = objectHolder.apply(request.serviceName());
        try {
            Method method = serviceBean.getClass().getMethod(request.methodName(),
                    ClassUtils.getClasses(request.parameters()));
            CacheableResponse response = new CacheableResponse(msg.getSecond());

            // 幂等性方法
            IdempotentService idempotentService = method.getAnnotation(IdempotentService.class);
            if (idempotentService != null && idempotentService.idempotence()) {
                response.signIdempotent();
                return new Pair<BasicRequest, CacheableResponse>(request, response);
            }

            // 可缓存方法
            CacheableService cacheableService = method.getAnnotation(CacheableService.class);
            if (cacheableService != null) {
                response.setExpireTime(cacheableService.exprie());
                if (cacheableService.consumer())
                    response.canCacheForConsumer();
                if (cacheableService.provider())
                    response.canCacheForProvider();
                return new Pair<BasicRequest, CacheableResponse>(request, response);
            }

        } catch (NoSuchMethodException | SecurityException e) {
            LOGGER.debug(e.getMessage());
        }
        return null;
    }

}