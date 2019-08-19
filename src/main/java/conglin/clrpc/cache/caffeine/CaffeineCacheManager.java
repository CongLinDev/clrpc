package conglin.clrpc.cache.caffeine;

import java.util.concurrent.TimeUnit;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;

import org.checkerframework.checker.index.qual.NonNegative;
import org.checkerframework.checker.nullness.qual.NonNull;

import conglin.clrpc.cache.AbstractCacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

public class CaffeineCacheManager extends AbstractCacheManager<BasicRequest, BasicResponse>{

    protected final Cache<BasicRequest, BasicResponse> cache;

    public CaffeineCacheManager(){
        super();
        cache = Caffeine.newBuilder()
            .initialCapacity(INITIAL_CAPACITY)
            .maximumSize(MAX_SIZE)
            .expireAfterWrite(MAX_EXPIRE_TIME, TimeUnit.MILLISECONDS)
            .expireAfter(new Expiry<BasicRequest, BasicResponse>() {

                    @Override
                    public long expireAfterCreate(@NonNull BasicRequest key, @NonNull BasicResponse value,
                            long currentTime) {
                            return TimeUnit.MILLISECONDS.toNanos(value.getExpireTime());
                    }

                    @Override
                    public long expireAfterUpdate(@NonNull BasicRequest key, @NonNull BasicResponse value,
                            long currentTime, @NonNegative long currentDuration) {
                        return 0;
                    }

                    @Override
                    public long expireAfterRead(@NonNull BasicRequest key, @NonNull BasicResponse value,
                            long currentTime, @NonNegative long currentDuration) {
                        return 0;
                    }
            })
            .build();
    }


    public void put(BasicRequest key, BasicResponse value) {
        cache.put(key, value);
    }


    public BasicResponse get(BasicRequest key) {
        return cache.getIfPresent(key);
    }
}