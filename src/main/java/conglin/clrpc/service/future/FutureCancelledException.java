package conglin.clrpc.service.future;

import conglin.clrpc.common.exception.RpcServiceException;

public class FutureCancelledException extends RpcServiceException {

    private static final long serialVersionUID = -6235027907224439419L;

    private final RpcFuture FUTURE;

    public FutureCancelledException(RpcFuture future){
        this.FUTURE = future;
    }

    public RpcFuture getFuture(){
        return FUTURE;
    }
}