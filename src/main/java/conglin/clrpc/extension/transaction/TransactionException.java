package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.exception.ServiceException;

public class TransactionException extends ServiceException {

    public TransactionException(String message) {
        super(message);
    }
}
