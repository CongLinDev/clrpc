package conglin.clrpc.service.future;

import conglin.clrpc.common.exception.RpcServiceException;

public class FutureCancelledException extends RpcServiceException {

    private static final long serialVersionUID = -6235027907224439419L;

    private final RpcFuture FUTURE;

    public FutureCancelledException(RpcFuture future) {
        this.FUTURE = future;
    }

    /**
     * 获取绑定的 {@link RpcFuture}
     * 
     * @return
     */
    public RpcFuture getFuture() {
        return FUTURE;
    }
}