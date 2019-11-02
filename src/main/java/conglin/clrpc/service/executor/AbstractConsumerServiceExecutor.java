package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;

import javax.security.auth.DestroyFailedException;

import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

abstract public class AbstractConsumerServiceExecutor implements ServiceExecutor<BasicRequest> {

    private final ExecutorService executor;

    private final CacheManager<BasicRequest, BasicResponse> cacheManager;

    public AbstractConsumerServiceExecutor(ExecutorService executor) {
        this(executor, null);
    }

    public AbstractConsumerServiceExecutor(ExecutorService executor,
            CacheManager<BasicRequest, BasicResponse> cacheManager){
        this.executor = executor;
        this.cacheManager = cacheManager;
    }


    /**
     * 处理收到的回复
     * @param response
     */
    abstract public void receiveResponse(BasicResponse response);
    
    @Override
    public ExecutorService getExecutorService() {
        return executor;
    }

    @Override
    public void destroy() throws DestroyFailedException {
        // do nothing
    }

    @Override
    public boolean isDestroyed() {
        return false;
    }

}