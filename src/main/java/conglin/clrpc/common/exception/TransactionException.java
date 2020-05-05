package conglin.clrpc.common.exception;

/**
 * 事务异常
 */
public class TransactionException extends RuntimeException {

    private static final long serialVersionUID = 4793530560261525314L;

    public TransactionException(String desc) {
        super(desc);
    }
}