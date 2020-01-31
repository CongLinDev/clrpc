package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;

public class BasicObjectProxy extends AbstractProxy implements ObjectProxy, InvocationHandler {

    // 代理服务名
    protected final String serviceName;

    public BasicObjectProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender, identifierGenerator);
        this.serviceName = serviceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if (Object.class == method.getDeclaringClass()) {
            switch (method.getName()) {
            case "equals":
                return proxy == args[0];
            case "hashCode":
                return System.identityHashCode(proxy);
            case "toString":
                return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
                        + ", with InvocationHandler " + this;
            default:
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        RpcFuture future = call(method, args);
        return future.get();
    }

    @Override
    public RpcFuture call(String methodName, Object... args) {
        return super.doCall(serviceName, methodName, args);
    }

    @Override
    public RpcFuture call(String remoteAddress, String methodName, Object... args) {
        return super.doCall(remoteAddress, serviceName, methodName, args);
    }

    @Override
    public RpcFuture call(Method method, Object... args) {
        return super.doCall(serviceName, method, args);
    }

    @Override
    public RpcFuture call(String remoteAddress, Method method, Object... args) {
        return super.doCall(remoteAddress, serviceName, method, args);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T convert(Class<T> interfaceClass) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass }, this);
    }

    /**
     * 获得绑定的服务名
     * 
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }
}
