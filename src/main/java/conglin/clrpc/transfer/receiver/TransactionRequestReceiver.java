package conglin.clrpc.transfer.receiver;

import conglin.clrpc.common.exception.TransactionException;
import conglin.clrpc.common.util.atomic.TransactionHelper;
import conglin.clrpc.common.util.atomic.ZooKeeperTransactionHelper;
import conglin.clrpc.service.ProviderServiceHandler;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.message.TransactionRequest;

public class TransactionRequestReceiver extends BasicRequestReceiver {

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
            helper.watch(transactionRequest.getRequestId());

            BasicResponse response = super.handleRequest(transactionRequest);
            if(!response.isError()) return response;

            helper.reparepare(transactionRequest.getRequestId(), transactionRequest.getSerialNumber());
            return null;

        }catch(TransactionException e){
            return null;
        }
    }

    @Override
    public void destory() {
        super.destory();
        helper.destroy();
    }
}