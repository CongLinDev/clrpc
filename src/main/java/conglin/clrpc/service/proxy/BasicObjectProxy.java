package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchProviderException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.sender.RequestSender;

public class BasicObjectProxy implements ObjectProxy, InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(BasicObjectProxy.class);

    private final String serviceName;
    private final RequestSender sender;
    private final IdentifierGenerator identifierGenerator;

    public BasicObjectProxy(String serviceName, RequestSender sender, IdentifierGenerator identifierGenerator){
        this.serviceName = serviceName;
        this.sender = sender;
        this.identifierGenerator = identifierGenerator;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class == method.getDeclaringClass()){
            switch (method.getName()){
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return proxy.getClass().getName() + "@" +
                        Integer.toHexString(System.identityHashCode(proxy)) +
                        ", with InvocationHandler " + this;
                default:
                    throw new IllegalStateException(String.valueOf(method));
            }
        }
        
        RpcFuture future = call(method, args);
        return future.get();
    }

    @Override
    public RpcFuture call(String methodName, Object... args) {
        BasicRequest request = new BasicRequest();
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));

        request.setRequestId(identifierGenerator.generateIndentifier(methodName));

        log.debug(request.toString());
        return sender.sendRequest(request);
    }

    @Override
    public RpcFuture call(String remoteAddress, String methodName, Object... args) throws NoSuchProviderException {
        BasicRequest request = new BasicRequest();
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));

        request.setRequestId(identifierGenerator.generateIndentifier(methodName));
        
        log.debug(request.toString());
        return sender.sendRequest(remoteAddress, request);
    }

    @Override
    public RpcFuture call(Method method, Object... args) {
        BasicRequest request = new BasicRequest();
        request.setServiceName(serviceName);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        request.setRequestId(identifierGenerator.generateIndentifier(method.getName()));

        log.debug(request.toString());
        return sender.sendRequest(request);
    }

    @Override
    public RpcFuture call(String remoteAddress, Method method, Object... args) throws NoSuchProviderException {
        BasicRequest request = new BasicRequest();
        request.setServiceName(serviceName);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        request.setRequestId(identifierGenerator.generateIndentifier(method.getName()));
        
        log.debug(request.toString());
        return sender.sendRequest(remoteAddress, request);
    }

    protected Class<?>[] getClassType(Object[] objs){
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = objs[i].getClass();
        }
        return types;
    }
}

