package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.common.exception.NoSuchProviderException;
import conglin.clrpc.service.future.RpcFuture;

/**
 * 针对服务消费者的对象代理
 */
public interface ObjectProxy {
    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param methodName 方法名
     * @param args       参数
     * @return
     */
    RpcFuture call(String methodName, Object... args);

    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param method 方法
     * @param args   参数
     * @return
     */
    RpcFuture call(Method method, Object... args);

    /**
     * 异步调用函数 指定服务提供者的地址 建议在 {@link Callback#fail(String, Exception)} 中使用该方法进行重试或回滚
     * 而不应该在一般的调用时使用该方法
     * 
     * @param remoteAddress 指定远程地址
     * @param methodName    方法名
     * @param args          参数
     * @return
     * @throws NoSuchProviderException
     */
    RpcFuture call(String remoteAddress, String methodName, Object... args) throws NoSuchProviderException;

    /**
     * 异步调用函数 指定服务提供者的地址 建议在 {@link Callback#fail(String, Exception)} 中使用该方法进行重试或回滚
     * 而不应该在一般的调用时使用该方法
     * 
     * @param remoteAddress 指定远程地址
     * @param method        方法
     * @param args          参数
     * @return
     * @throws NoSuchProviderException
     */
    RpcFuture call(String remoteAddress, Method method, Object... args) throws NoSuchProviderException;

}