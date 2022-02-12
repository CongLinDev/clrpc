package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.exception.RpcServiceException;

public class TransactionException extends RpcServiceException {

    public TransactionException(String message) {
        super(message);
    }
}
