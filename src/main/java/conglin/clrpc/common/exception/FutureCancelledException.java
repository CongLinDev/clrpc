package conglin.clrpc.common.exception;

import conglin.clrpc.service.future.RpcFuture;

public class FutureCancelledException extends Exception {

    private static final long serialVersionUID = -6235027907224439419L;

    private final RpcFuture future;

    public FutureCancelledException(RpcFuture future){
        this.future = future;
    }

    public RpcFuture getFuture(){
        return future;
    }
}