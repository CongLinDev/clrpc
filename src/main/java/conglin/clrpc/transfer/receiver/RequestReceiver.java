package conglin.clrpc.transfer.receiver;

import conglin.clrpc.cache.CacheManager;
import conglin.clrpc.common.util.threadpool.ThreadPool;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

public interface RequestReceiver extends ThreadPool{
    
    /**
     * 初始化
     * @param serviceHandler
     */
    void init(ProviderServiceHandler serviceHandler);

    /**
     * 注册缓冲池
     * @param cacheManager
     */
    void registerCachePool(CacheManager<BasicRequest, BasicResponse> cacheManager);


    /**
     * 处理请求
     * @param request
     * @return
     */
    BasicResponse handleRequest(BasicRequest request);

    /**
     * 关闭请求接收器
     */
    void stop();
}