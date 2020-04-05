package conglin.clrpc.common.exception;

import java.util.concurrent.ExecutionException;

/**
 * RPC服务异常
 */
public class RpcServiceException extends ExecutionException {

    private static final long serialVersionUID = -761328557352237488L;

    public RpcServiceException() {

    }

    public RpcServiceException(String desc) {
        super(desc);
    }

    public RpcServiceException(String message, Throwable cause) {
        super(message, cause);
    }

    public RpcServiceException(Throwable cause) {
        super(cause);
    }
}