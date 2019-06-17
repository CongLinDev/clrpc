package conglin.clrpc.service.proxy;

import conglin.clrpc.common.util.concurrent.RpcFuture;

public interface ObjectProxy {
    
    RpcFuture call(String function, Object... args);
}