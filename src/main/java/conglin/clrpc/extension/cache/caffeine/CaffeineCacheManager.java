package conglin.clrpc.extension.cache.caffeine;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.extension.cache.AbstractCacheManager;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.CacheableResponse;

public class CaffeineCacheManager extends AbstractCacheManager<BasicRequest, CacheableResponse>{

    protected final Cache<BasicRequest, CacheableResponse> cache;

    public CaffeineCacheManager(PropertyConfigurer configurer){
        super(configurer);
        cache = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CAPACITY)
            .maximumSize(MAX_SIZE)
            .expireAfterWrite(MAX_EXPIRE_TIME, TimeUnit.MILLISECONDS)
            .expireAfter(new Expiry<BasicRequest, CacheableResponse>() {

                    @Override
                    public long expireAfterCreate(@NonNull BasicRequest key, @NonNull CacheableResponse value,
                            long currentTime) {
                            return TimeUnit.MILLISECONDS.toNanos(value.getExpireTime());
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull BasicRequest key, @NonNull CacheableResponse value,
                            long currentTime, @NonNegative long currentDuration) {
                        return 0;
                    }

                    @Override
                    public long expireAfterRead(@NonNull BasicRequest key, @NonNull CacheableResponse value,
                            long currentTime, @NonNegative long currentDuration) {
                        return 0;
                    }
            })
            .build();
    }

    @Override
    public void put(BasicRequest key, CacheableResponse value) {
        cache.put(key, value);
    }

    @Override
    public CacheableResponse get(BasicRequest key) {
        return cache.getIfPresent(key);
    }

    @Override
    public void clear() {
        cache.cleanUp();
    }
}