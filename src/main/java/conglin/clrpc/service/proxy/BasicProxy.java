package conglin.clrpc.service.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.annotation.AnnotationParser;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.BasicRequest;

/**
 * 基本的代理
 * 
 * 适合未知服务名的调用
 */
public class BasicProxy extends CommonProxy implements InvocationHandler {

    // ID生成器
    private final IdentifierGenerator identifierGenerator;

    public BasicProxy(RequestSender sender, IdentifierGenerator identifierGenerator) {
        super(sender);
        this.identifierGenerator = identifierGenerator;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        if (Object.class == methodDeclaringClass) {
            switch (methodName) {
                case "equals":
                    return proxy == args[0];
                case "hashCode":
                    return System.identityHashCode(proxy);
                case "toString":
                    return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
                            + ", with InvocationHandler " + this;
                default:
                    throw new IllegalStateException(methodName);
            }
        }

        RpcFuture future = call(getServiceName(methodDeclaringClass), methodName, args);
        Object result = handleFuture(future);
        return result == null ? ClassUtils.defaultValue(method.getReturnType()) : result;
    }

    /**
     * 获取服务名
     * 
     * @param methodDeclaringClass
     * @return
     */
    protected String getServiceName(Class<?> methodDeclaringClass) {
        String serviceName = AnnotationParser.serviceName(methodDeclaringClass);
        if (serviceName == null) {
            throw new IllegalStateException("Cannot find available serviceName from " + methodDeclaringClass.getName());
        }
        return serviceName;
    }

    /**
     * 处理 future
     * 
     * @param future
     * @return
     * @throws Exception
     */
    protected Object handleFuture(RpcFuture future) throws Exception {
        return future.get();
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
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName), serviceName, methodName, args);
        return super.call(request);
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
    public RpcFuture callWith(String remoteAddress, String serviceName, String methodName, Object... args) {
        BasicRequest request = new BasicRequest(identifierGenerator.generate(methodName), serviceName, methodName, args);
        return super.callWith(remoteAddress, request);
    }

    /**
     * 标识符生成器
     * 
     * @return
     */
    protected IdentifierGenerator identifierGenerator() {
        return this.identifierGenerator;
    }
}