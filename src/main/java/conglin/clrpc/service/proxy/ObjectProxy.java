package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.common.util.concurrent.RpcFuture;

public interface ObjectProxy {
    
    RpcFuture call(String methodName, Object... args);

    RpcFuture call(Method method, Object... args);
}