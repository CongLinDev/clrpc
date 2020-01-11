package conglin.clrpc.service.proxy;

import java.lang.reflect.Method;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchProviderException;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.BasicRequest;

abstract public class AbstractProxy {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractProxy.class);

    // 发送器
    protected final RequestSender sender;
    // ID生成器
    protected final IdentifierGenerator identifierGenerator;

    public AbstractProxy(RequestSender sender, IdentifierGenerator identifierGenerator) {
        this.sender = sender;
        this.identifierGenerator = identifierGenerator;
    }

    /**
     * 按顺序返回对象数组所对应的类对象
     * 
     * @param objs
     * @return 类对象数组
     */
    protected Class<?>[] getClassType(Object[] objs) {
        return ClassUtils.getClasses(objs);
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
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName));
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));

        LOGGER.debug(request.toString());
        return sender.sendRequest(request);
    }

    /**
     * 异步调用函数 指定服务提供者的地址 建议在 {@link Callback#fail(String, Exception)} 中使用该方法进行重试或回滚
     * 而不应该在一般的调用时使用该方法
     * 
     * @param remoteAddress 指定远程地址
     * @param serviceName   服务名
     * @param methodName    方法名
     * @param args          参数
     * @return future
     * @throws NoSuchProviderException
     */
    protected RpcFuture doCall(String remoteAddress, String serviceName, String methodName, Object... args)
            throws NoSuchProviderException {
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName));
        request.setServiceName(serviceName);
        request.setMethodName(methodName);
        request.setParameters(args);
        request.setParameterTypes(getClassType(args));

        LOGGER.debug(request.toString());
        return sender.sendRequest(remoteAddress, request);
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
        BasicRequest request = new BasicRequest(identifierGenerator.generate(method.getName()));
        request.setServiceName(serviceName);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        LOGGER.debug(request.toString());
        return sender.sendRequest(request);
    }

    /**
     * 异步调用函数 指定服务提供者的地址 建议在 {@link Callback#fail(String, Exception)} 中使用该方法进行重试或回滚
     * 而不应该在一般的调用时使用该方法
     * 
     * @param remoteAddress 指定远程地址
     * @param serviceName   服务名
     * @param method        方法
     * @param args          参数
     * @return future
     * @throws NoSuchProviderException
     */
    protected RpcFuture doCall(String remoteAddress, String serviceName, Method method, Object... args)
            throws NoSuchProviderException {
        BasicRequest request = new BasicRequest(identifierGenerator.generate(method.getName()));
        request.setServiceName(serviceName);
        request.setMethodName(method.getName());
        request.setParameters(args);
        request.setParameterTypes(method.getParameterTypes());

        LOGGER.debug(request.toString());
        return sender.sendRequest(remoteAddress, request);
    }
}