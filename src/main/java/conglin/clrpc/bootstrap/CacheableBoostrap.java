package conglin.clrpc.bootstrap;

import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.cache.caffeine.CaffeineCacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

abstract public class CacheableBoostrap{
    protected final boolean ENABLE_CACHE;
    protected final CacheManager<BasicRequest, BasicResponse> cacheManager;

    public CacheableBoostrap(boolean enableCache){
        this.ENABLE_CACHE = enableCache;
        if(ENABLE_CACHE){
            cacheManager = new CaffeineCacheManager();
        }else{
            cacheManager = null;
        }
    }
}