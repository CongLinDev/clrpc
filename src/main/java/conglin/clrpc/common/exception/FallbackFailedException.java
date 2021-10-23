package conglin.clrpc.common.exception;

import java.io.Serial;

public class FallbackFailedException extends RpcServiceException {

    @Serial
    private static final long serialVersionUID = -115938102211662316L;

    public FallbackFailedException(Throwable throwable) {
        super("Fallback failed.", throwable);
    }
}