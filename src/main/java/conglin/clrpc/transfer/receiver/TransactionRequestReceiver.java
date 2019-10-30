package conglin.clrpc.transfer.receiver;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.atomic.TransactionHelper;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.message.TransactionRequest;

public class TransactionRequestReceiver extends BasicRequestReceiver {

    private static final Logger log = LoggerFactory.getLogger(TransactionRequestReceiver.class);

    protected TransactionHelper helper;

    @Override
    public void init(ProviderServiceHandler serviceHandler) {
        helper = new ZooKeeperTransactionHelper();
        super.init(serviceHandler);
    }

    @Override
    public BasicResponse handleRequest(BasicRequest request) {
        TransactionRequest transactionRequest = (TransactionRequest) request;

        if(!helper.sign(transactionRequest.getRequestId(), transactionRequest.getSerialNumber()))
            return null;

        try{
            if(!helper.watch(transactionRequest.getRequestId())){
                log.debug("Request (id="+ transactionRequest.getRequestId() +") cancelled");
                BasicResponse response = new BasicResponse();
                response.setRequestId(transactionRequest.getRequestId());
                response.signError();
                response.setResult("Request cancelled.");
                return response;
            }

            BasicResponse response = super.handleRequest(transactionRequest);
            if(!response.isError()) return response;

            helper.reparepare(transactionRequest.getRequestId(), transactionRequest.getSerialNumber());
            return null;
            
        }catch(TransactionException e){
            log.error(e.getMessage());
            return null;
        }
    }


    @Override
    public void destory() {
        super.destory();
        helper.destroy();
    }

    @Override
    public boolean canHandle(BasicRequest task) {
        return task.getClass() == TransactionRequest.class;
    }

    @Override
    public void doHandle(BasicRequest task) {
        handleRequest(task);
    }

}