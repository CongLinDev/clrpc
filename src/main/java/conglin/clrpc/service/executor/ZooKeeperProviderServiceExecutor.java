package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;
import java.util.function.Function;

import javax.security.auth.DestroyFailedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.util.atomic.TransactionHelper;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.message.TransactionRequest;

public class ZooKeeperProviderServiceExecutor extends BasicProviderServiceExecutor {

    private static final Logger log = LoggerFactory.getLogger(ZooKeeperProviderServiceExecutor.class);

    protected TransactionHelper helper;
    
    public ZooKeeperProviderServiceExecutor(Function<String, Object> serviceObjectsHolder,
            ExecutorService executor, CacheManager<BasicRequest, BasicResponse> cacheManager){
        super(serviceObjectsHolder, executor, cacheManager);
        helper = new ZooKeeperTransactionHelper();
    }

    @Override
    protected boolean doExecute(BasicRequest request, BasicResponse response)
            throws UnsupportedServiceException, ServiceExecutionException {

        // 该类被设计为异步实现，一定返回 false
        
        TransactionRequest transactionRequest = (TransactionRequest) request;
        
        // 标记事务的本条请求被当前服务提供者所占有
        if(!helper.sign(transactionRequest.getRequestId(), transactionRequest.getSerialNumber()))
            return false;
        
        try{
            helper.watchAsync(transactionRequest.getRequestId(), new Callback<Object>(){
                @Override
                public void success(Object result) {
                    
                    try{
                        ZooKeeperProviderServiceExecutor.super.doExecute(request, response);
                    }catch(UnsupportedServiceException | ServiceExecutionException e){
                        log.error("Request failed: " + e.getMessage());

                        // 重新标记，由服务消费者在下次定时轮询时重新请求
                        helper.reparepare(transactionRequest.getRequestId(), transactionRequest.getSerialNumber());
                        // send nothing
                        return;
                    }

                    sendResponse(response);

                    log.info("Transaction id=" + transactionRequest.getRequestId() +
                    " serialNumber=" + transactionRequest.getSerialNumber() + " has executed.");
                }

                @Override
                public void fail(Exception exception) {
                    // do nothing
                    log.info("Transaction id=" + transactionRequest.getRequestId() +
                        " serialNumber=" + transactionRequest.getSerialNumber() + " has cancelled.");
                }
            });
        } catch (TransactionException e){
            log.error(e.getMessage());
        }
        return false;
    }
    

    @Override
    public void destroy() throws DestroyFailedException {
        helper.destroy();
    }

    @Override
    public boolean isDestroyed() {
        return helper.isDestroyed();
    }
}