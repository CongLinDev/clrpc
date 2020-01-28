package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.exception.UnsupportedServiceException;
import conglin.clrpc.common.util.atomic.TransactionHelper;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.message.BasicResponse;
import conglin.clrpc.transport.message.TransactionRequest;

public class ProviderTransactionServiceChannelHandler
        extends ProviderAbstractServiceChannelHandler<TransactionRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderTransactionServiceChannelHandler.class);

    protected final TransactionHelper helper;

    public ProviderTransactionServiceChannelHandler(ProviderContext context) {
        super(context);
        helper = new ZooKeeperTransactionHelper(context.getPropertyConfigurer());
    }

    @Override
    protected Object execute(TransactionRequest msg) {
        return doExecute(msg);
    }

    /**
     * 执行请求具体方法
     * 
     * @param request
     * @return
     */
    protected Object doExecute(TransactionRequest request) {

        // 标记事务的本条请求被当前服务提供者所占有
        long transactionId = request.getTransactionId();
        int serialId = request.getSerialId();
        LOGGER.debug("Receive transaction transactionId={}", transactionId);

        if (!helper.sign(transactionId, serialId)) {
            LOGGER.debug("Ignore transaction transactionId={}", transactionId);
            return null;
        }

        try {
            helper.watch(transactionId, new Callback() {
                @Override
                public void success(Object result) {

                    try {
                        BasicResponse response = ProviderTransactionServiceChannelHandler.super.doExecute(request);
                        next(request, response);
                        LOGGER.debug("Transaction transactionId={} serialId={} has executed.", transactionId, serialId);
                    } catch (UnsupportedServiceException | ServiceExecutionException e) {
                        LOGGER.error("Request failed: {}", e.getMessage());

                        // 重新标记，由服务消费者在下次定时轮询时重新请求
                        helper.reparepare(transactionId, serialId);
                    }
                }

                @Override
                public void fail(Exception exception) {
                    LOGGER.debug("Transaction transactionId={} serialId={} has cancelled.", transactionId, serialId);
                    // 返回错误回复
                    BasicResponse response = new BasicResponse(request.getRequestId());
                    response.signError();
                    response.setResult(new TransactionException("Transaction has been cancelled."));

                    next(request, response);
                }
            });
        } catch (TransactionException e) {
            LOGGER.error(e.getMessage());
        }
        return null;
    }

}