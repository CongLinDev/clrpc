package conglin.clrpc.extension.transaction;

import conglin.clrpc.common.ServiceException;

public class TransactionException extends ServiceException {

    public TransactionException(String message) {
        super(message);
    }
}
