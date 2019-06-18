package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.common.util.lang.ClassUtil;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.BasicRequest;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;



public class BasicObjectProxy<T> implements ObjectProxy, InvocationHandler {
    private static final Logger log = LoggerFactory.getLogger(BasicObjectProxy.class);

    private Class<T> clazz;

    private ClientServiceHandler serviceHandler;

    private ClientTransfer clientTransfer;

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
            } else{
                throw new IllegalStateException(String.valueOf(method));
            }
        }

        //创建发送请求消息
        BasicRequest request = BasicRequest.builder()
                .requestId(UUID.randomUUID().toString())
                .className(method.getDeclaringClass().getName())
                .methodName(method.getName())
                .parameterTypes(method.getParameterTypes())
                .parameters(args)
                .build();
        log.debug(request.toString());
        // send request
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler();
        RpcFuture future = serviceHandler.sendRequest(request, channelHandler.getChannel());
        return future.get();
    }

    @Override
    public RpcFuture call(String methodName, Object... args) {
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler();
        BasicRequest request = createRequest(this.clazz.getName(), methodName, args);
        return serviceHandler.sendRequest(request, channelHandler.getChannel());
    }

    @Override
    public RpcFuture call(Method method, Object... args) {
        return call(method.getName(), args);
    }

    private BasicRequest createRequest(String className, String methodName, Object[] args){

        //构造请求
        BasicRequest request = BasicRequest.builder()
                    .requestId(UUID.randomUUID().toString())
                    .className(className)
                    .methodName(methodName)
                    .parameters(args)
                    .parameterTypes(ClassUtil.getClassType(args))
                    .build();
        log.debug(request.toString());
        return request;
    }

}

