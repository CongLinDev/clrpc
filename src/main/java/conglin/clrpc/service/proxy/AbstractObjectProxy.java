package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;

abstract public class AbstractObjectProxy extends CommonProxy implements InvocationHandler {

    // 代理服务名
    protected final String serviceName;

    public AbstractObjectProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender, identifierGenerator);
        this.serviceName = serviceName;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        if (Object.class == method.getDeclaringClass()) {
            switch (methodName) {
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

        RpcFuture future = call(serviceName, methodName, args);
        Object result = handleFuture(future);
        return result == null ? ClassUtils.defaultValue(method.getReturnType()) : result;
    }

    /**
     * 处理 future
     * @param future
     * @return
     * @throws Exception
     */
    abstract protected Object handleFuture(RpcFuture future) throws Exception;

    /**
     * 获得绑定的服务名
     * 
     * @return
     */
    public String getServiceName() {
        return serviceName;
    }
}
