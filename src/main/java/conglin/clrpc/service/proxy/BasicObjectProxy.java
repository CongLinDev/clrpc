package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchServerException;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.sender.RequestSender;

public class BasicObjectProxy implements ObjectProxy, InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(BasicObjectProxy.class);

    private final String serviceName;
    private final RequestSender sender;

    public BasicObjectProxy(String serviceName, RequestSender sender){
        this.serviceName = serviceName;
        this.sender = sender;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        if(Object.class == method.getDeclaringClass()){
            String name = method.getName();
            if ("equals".equals(name)) {
                return proxy == args[0];
            } else if("hashCode".equals(name)){
                return System.identityHashCode(proxy);
            } else if ("toString".equals(name)) {
                return proxy.getClass().getName() + "@" +
                    Integer.toHexString(System.identityHashCode(proxy)) +
                    ", with InvocationHandler " + this;
            } else {
                throw new IllegalStateException(String.valueOf(method));
            }
            // switch (name){
            //     case "equals":
            //         return proxy == args[0];
            //     case "hashCode":
            //         return System.identityHashCode(proxy);
            //     case "toString":
            //         return proxy.getClass().getName() + "@" +
            //             Integer.toHexString(System.identityHashCode(proxy)) +
            //             ", with InvocationHandler " + this;
            //     default:
            //         throw new IllegalStateException(String.valueOf(method));
            // }
        }
        
        RpcFuture future = call(method, args);
        return future.get();
    }

    @Override
    public RpcFuture call(String methodName, Object... args) {
        BasicRequest request = BasicRequest.builder()
            .methodName(methodName)
            .parameters(args)
            .parameterTypes(getClassType(args))
            .serviceName(serviceName)
            .build();
        log.debug(request.toString());
        return sender.sendRequest(request);
    }

    @Override
    public RpcFuture call(String remoteAddress, String methodName, Object... args) throws NoSuchServerException {
        BasicRequest request = BasicRequest.builder()
            .methodName(methodName)
            .parameters(args)
            .parameterTypes(getClassType(args))
            .serviceName(serviceName)
            .build();
        log.debug(request.toString());
        return sender.sendRequest(remoteAddress, request);
    }

    @Override
    public RpcFuture call(Method method, Object... args) {
        BasicRequest request = BasicRequest.builder()
            .methodName(method.getName())
            .parameterTypes(method.getParameterTypes())
            .parameters(args)
            .serviceName(this.serviceName)
            .build();
        log.debug(request.toString());
        return sender.sendRequest(request);
    }

    @Override
    public RpcFuture call(String remoteAddress, Method method, Object... args) throws NoSuchServerException {
        BasicRequest request = BasicRequest.builder()
            .methodName(method.getName())
            .parameterTypes(method.getParameterTypes())
            .parameters(args)
            .serviceName(this.serviceName)
            .build();
        log.debug(request.toString());
        return sender.sendRequest(remoteAddress, request);
    }

    private Class<?>[] getClassType(Object[] objs){
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = objs[i].getClass();
        }
        return types;
    }
}

