package conglin.clrpc.common.exception;

import java.io.Serial;
import java.util.concurrent.ExecutionException;

/**
 * RPC服务异常
 */
public class ServiceException extends ExecutionException {

    @Serial
    private static final long serialVersionUID = -761328557352237488L;

    public ServiceException() {

    }

    public ServiceException(String desc) {
        super(desc);
    }

    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public ServiceException(Throwable cause) {
        super(cause);
    }
}