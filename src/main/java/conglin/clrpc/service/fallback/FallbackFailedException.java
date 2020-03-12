package conglin.clrpc.service.fallback;

import conglin.clrpc.common.exception.RpcServiceException;

public class FallbackFailedException extends RpcServiceException {

    private static final long serialVersionUID = -115938102211662316L;

    public FallbackFailedException(Throwable throwable) {
        super("Fallback failed.", throwable);
    }
}