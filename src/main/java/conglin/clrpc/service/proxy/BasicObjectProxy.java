package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;

public class BasicObjectProxy<T> implements ObjectProxy, InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(BasicObjectProxy.class);

    private final String serviceName;
    private final ClientServiceHandler serviceHandler;

    public BasicObjectProxy(String serviceName, ClientServiceHandler serviceHandler){
        this.serviceName = serviceName;
        this.serviceHandler = serviceHandler;
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
        }
        
        RpcFuture future = call(method, args);
        return future.get();
    }

    @Override
    public RpcFuture call(String methodName, Object... args) {
        return callCore(serviceName, methodName, args);
    }

    @Override
    public RpcFuture call(Method method, Object... args) {
        return callCore(method, args);
    }

    /**
     * 请求核心函数(用于异步请求)
     * @param serviceName
     * @param methodName
     * @param args
     * @return
     */
    private RpcFuture callCore(String serviceName, String methodName, Object...args){
        BasicRequest request = BasicRequest.builder()
            .requestId(UUID.randomUUID().toString())
            //.className(null)
            .methodName(methodName)
            .parameters(args)
            .parameterTypes(getClassType(args))
            .serviceName(serviceName)
            .build();
        log.debug(request.toString());
        return serviceHandler.sendRequest(request);
    }

    /**
     * 请求核心函数(用于同步请求)
     * @param method
     * @param args
     * @return
     */
    private RpcFuture callCore(Method method, Object... args){
        BasicRequest request = BasicRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .className(method.getDeclaringClass().getName())
            .methodName(method.getName())
            .parameterTypes(method.getParameterTypes())
            .parameters(args)
            .serviceName(this.serviceName)
            .build();
        log.debug(request.toString());
        return serviceHandler.sendRequest(request);
    }

    private Class<?>[] getClassType(Object[] objs){
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = objs[i].getClass();
        }
        return types;
    }
}

