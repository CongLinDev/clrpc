package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * RPC proxy 的类型
 */
public enum ProxyType {
    Common, Basic, Sync, Async, Transaction, Unknown;

    public static ProxyType type(Object proxyObject) {
        if (proxyObject instanceof RpcProxy) {
            Class<?> clazz = proxyObject.getClass();
            if (BasicProxy.class == clazz) {
                return Basic;
            } else if (SyncObjectProxy.class.isAssignableFrom(clazz)) {
                return Sync;
            } else if (AsyncObjectProxy.class.isAssignableFrom(clazz)) {
                return Async;
            } else if (TransactionProxy.class.isAssignableFrom(clazz)) {
                return Transaction;
            }
        } else if (Proxy.isProxyClass(proxyObject.getClass())) {
            InvocationHandler handler = Proxy.getInvocationHandler(proxyObject);
            return type(handler);
        }
        return Unknown;
    }
}