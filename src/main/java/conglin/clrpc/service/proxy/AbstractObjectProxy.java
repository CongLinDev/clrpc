package conglin.clrpc.service.proxy;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.ServiceObjectHolder;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.instance.ServiceInstance;
import conglin.clrpc.service.instance.condition.InstanceCondition;
import conglin.clrpc.service.strategy.FailStrategy;
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
        Class<?> methodDeclaringClass = method.getDeclaringClass();
        if (Object.class == methodDeclaringClass) {
            return switch (method.getName()) {
                case "equals" -> proxy == args[0];
                case "hashCode" -> System.identityHashCode(proxy);
                case "toString" -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy))
                        + ", with InvocationHandler " + this;
                default -> throw new IllegalStateException(method.getName());
            };
        }

        InvocationContext invocationContext = call(getServiceName(methodDeclaringClass), getMethodName(method), args);
        Object result = handleContext(invocationContext);
        if (result == null) {
            return ClassUtils.defaultValue(method.getReturnType());
        }
        return result;
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
     * 获取方法名
     * 
     * @param method
     * @return
     */
    protected String getMethodName(Method method) {
        return ServiceObjectHolder.ServiceObjectWrapper.customMethodName(method);
    }

    /**
     * 异步调用函数 使用负载均衡策略
     * 
     * @param serviceName 服务名
     * @param methodName  方法名
     * @param args        参数
     * @return InvocationContext
     */
    public InvocationContext call(String serviceName, String methodName, Object... args) {
        InvocationContext invocationContext = new InvocationContext();
        invocationContext.setRequest(new RequestPayload(serviceName, methodName, args));
        invocationContext.setFailStrategy(failStrategy());
        invocationContext.setInstanceConsumer(instanceConsumer());
        invocationContext.setInstanceCondition(instanceCondition());
        invocationContext.setTimeoutThreshold(timeoutThreshold());
        super.call(invocationContext);
        return invocationContext;
    }

    /**
     * 处理 invocationContext
     * 
     * @param invocationContext
     * @return
     * @throws Exception
     */
    abstract protected Object handleContext(InvocationContext invocationContext) throws Exception;
    
    /**
     * 超时时间
     * 
     * @return 超时阈值 单位为 ms
     */
    abstract protected long timeoutThreshold();

    /**
     * fail strategy
     *
     * @return
     */
    abstract protected FailStrategy failStrategy();

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