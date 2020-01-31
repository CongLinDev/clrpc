package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;

/**
 * 通用的代理
 * 
 * 适合未知服务名的调用
 */
public class CommonProxy extends AbstractProxy {

    public CommonProxy(RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender, identifierGenerator);
    }

    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param serviceName 服务名
     * @param methodName  方法名
     * @param args        参数
     * @return future
     */
    public RpcFuture call(String serviceName, String methodName, Object... args) {
        return super.doCall(serviceName, methodName, args);
    }

    /**
     * 异步调用函数 指定服务提供者的地址 建议在 {@link Callback#fail(Exception)} 中使用该方法进行重试或回滚
     * 而不应该在一般的调用时使用该方法
     * 
     * @param remoteAddress 指定远程地址
     * @param serviceName   服务名
     * @param methodName    方法名
     * @param args          参数
     * @return future
     */
    public RpcFuture call(String remoteAddress, String serviceName, String methodName, Object... args) {
        return super.doCall(remoteAddress, serviceName, methodName, args);
    }

    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param serviceName 服务名
     * @param method      方法
     * @param args        参数
     * @return future
     */
    public RpcFuture call(String serviceName, Method method, Object... args) {
        return super.doCall(serviceName, method, args);
    }

    /**
     * 异步调用函数 指定服务提供者的地址 建议在 {@link Callback#fail(Exception)} 中使用该方法进行重试或回滚
     * 而不应该在一般的调用时使用该方法
     * 
     * @param remoteAddress 指定远程地址
     * @param serviceName   服务名
     * @param method        方法
     * @param args          参数
     * @return future
     */
    public RpcFuture call(String remoteAddress, String serviceName, Method method, Object... args) {
        return super.doCall(remoteAddress, serviceName, method, args);
    }

}