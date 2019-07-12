package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;



public class BasicObjectProxy<T> implements ObjectProxy, InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(BasicObjectProxy.class);

    private final Class<T> clazz;

    private final ClientServiceHandler serviceHandler;

    private final ClientTransfer clientTransfer;

    public BasicObjectProxy(Class<T> clazz, 
                    ClientServiceHandler serviceHandler,
                    ClientTransfer clientTransfer){
        this.clazz = clazz;
        this.serviceHandler = serviceHandler;
        this.clientTransfer = clientTransfer;
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
        String simpleClassName = this.clazz.getSimpleName();
        return callCore(simpleClassName, methodName, args);
    }

    @Override
    public RpcFuture call(Method method, Object... args) {
        return callCore(method, args);
    }

    /**
     * 请求核心函数
     * @param simpleClassName
     * @param methodName
     * @param args
     * @return
     */
    private RpcFuture callCore(String simpleClassName, String methodName, Object...args){
        BasicRequest request = BasicRequest.builder()
            .requestId(UUID.randomUUID().toString())
            .className(this.clazz.getName())
            .methodName(methodName)
            .parameters(args)
            .parameterTypes(getClassType(args))
            .build();
        log.debug(request.toString());
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(simpleClassName);
        return serviceHandler.sendRequest(request, channelHandler.getChannel());
    }

    /**
     * 请求核心函数
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
            .build();
        log.debug(request.toString());
        String simpleClassName = method.getDeclaringClass().getSimpleName();
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(simpleClassName);
        return serviceHandler.sendRequest(request, channelHandler.getChannel());
    }

    private Class<?>[] getClassType(Object[] objs){
        Class<?>[] types = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            types[i] = objs[i].getClass();
        }
        return types;
    }
}

