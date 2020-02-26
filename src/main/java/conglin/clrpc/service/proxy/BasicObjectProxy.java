package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.BasicRequest;

public class BasicObjectProxy extends AbstractProxy implements ObjectProxy, InvocationHandler {

    // 代理服务名
    protected final String serviceName;

    // ID生成器
    protected final IdentifierGenerator identifierGenerator;

    public BasicObjectProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender);
        this.serviceName = serviceName;
        this.identifierGenerator = identifierGenerator;
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

        RpcFuture future = call(methodName, args);
        return future.get();
    }

    @Override
    public RpcFuture call(String methodName, Object... args) {
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName));
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        return super.call(request);
    }

    @Override
    public RpcFuture call(String remoteAddress, String methodName, Object... args) {
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName));
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        return super.call(request, remoteAddress);
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
