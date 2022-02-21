package conglin.clrpc.service.proxy;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.service.future.strategy.FailStrategy;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.transport.message.RequestPayload;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Consumer;

/**
 * 基本的代理
 * 
 * 适合未知服务名的调用
 */
abstract public class AbstractObjectProxy extends SimpleProxy implements InvocationHandler {

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        String methodName = method.getName();
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        if (Object.class == methodDeclaringClass) {
            return switch (methodName) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
                        + ", with InvocationHandler " + this;
                default -> throw new IllegalStateException(methodName);
            };
        }

        InvocationFuture future = call(getServiceName(methodDeclaringClass), methodName, args);
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
        return methodDeclaringClass.getName();
    }

    /**
     * 处理 future
     * 
     * @param future
     * @return
     * @throws Exception
     */
    protected Object handleFuture(InvocationFuture future) throws Exception {
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
    public InvocationFuture call(String serviceName, String methodName, Object... args) {
        return call(instanceCondition(), new RequestPayload(serviceName, methodName, args));
    }

    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param instanceCondition
     * @param request 请求payload
     * @return future
     */
    public InvocationFuture call(InstanceCondition instanceCondition, RequestPayload request) {
        InvocationContext invocationContext = new InvocationContext();
        invocationContext.setRequest(request);
        invocationContext.setFailStrategyClass(failStrategyClass());
        invocationContext.setInstanceConsumer(instanceConsumer());
        invocationContext.setInstanceCondition(instanceCondition);
        return super.call(invocationContext);
    }

    /**
     * fail strategy
     *
     * @return
     */
    abstract protected Class<? extends FailStrategy> failStrategyClass();

    /**
     * instanceConsumer
     *
     * @return
     */
    abstract protected Consumer<ServiceInstance> instanceConsumer();

    /**
     * condition
     * 
     * @return
     */
    abstract protected InstanceCondition instanceCondition();
}