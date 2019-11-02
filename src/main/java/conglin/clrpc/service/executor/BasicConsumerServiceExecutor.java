package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;

public class BasicConsumerServiceExecutor extends AbstractConsumerServiceExecutor {

    protected final Function<Long, RpcFuture> futuresRemover;

    public BasicConsumerServiceExecutor(Function<Long, RpcFuture> futuresRemover,
        ExecutorService executor, CacheManager<BasicRequest, BasicResponse> cacheManager) {
        super(executor, cacheManager);
        this.futuresRemover = futuresRemover;
    }

    public BasicConsumerServiceExecutor(Function<Long, RpcFuture> futuresRemover,
        ExecutorService executor) {
        super(executor);
        this.futuresRemover = futuresRemover;
    }

    @Override
    public void execute(BasicRequest t) {
        // TODO Auto-generated method stub

    }

    // public RpcFuture sendRequest(BasicRequest request) {
    //     BasicFuture future = new BasicFuture(this, request);
    //     if(!putFuture(request, future)) return future;

    //     String addr = doSendRequest(request);
    //     future.setRemoteAddress(addr);
    //     return future;
    // }

    @Override
    public void receiveResponse(BasicResponse response) {
        Long requestId = response.getRequestId();
        //直接移除
        RpcFuture future = futuresRemover.apply(requestId);

        if(future != null){
            future.done(response);
        }

    }

    

}