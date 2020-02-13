package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.BasicRequest;

abstract public class AbstractProxy {

    // 发送器
    protected final RequestSender sender;
    // ID生成器
    protected final IdentifierGenerator identifierGenerator;

    public AbstractProxy(RequestSender sender, IdentifierGenerator identifierGenerator) {
        this.sender = sender;
        this.identifierGenerator = identifierGenerator;
    }

    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param serviceName 服务名
     * @param methodName  方法名
     * @param args        参数
     * @return future
     */
    protected RpcFuture doCall(String serviceName, String methodName, Object... args) {
        return doCall(null, serviceName, methodName, args);
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
    protected RpcFuture doCall(String remoteAddress, String serviceName, String methodName, Object... args) {
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName));
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        return sender.sendRequest(request, remoteAddress);
    }

    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param serviceName 服务名
     * @param method      方法
     * @param args        参数
     * @return future
     */
    protected RpcFuture doCall(String serviceName, Method method, Object... args) {
        return doCall(serviceName, method.getName(), args);
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
    protected RpcFuture doCall(String remoteAddress, String serviceName, Method method, Object... args) {
        return doCall(remoteAddress, serviceName, method.getName(), args);
    }
}