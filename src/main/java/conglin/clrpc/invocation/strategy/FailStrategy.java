package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.message.ResponsePayload;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;

/**
 * 失败策略
 */
public interface FailStrategy {
    /**
     * 没有目标
     * 
     * @param context
     * @param exception
     */
    void noTarget(InvocationContext context, NoAvailableServiceInstancesException exception);

    /**
     * 发生超时
     * 
     * @param context
     */
    void timeout(InvocationContext context);

    /**
     * 发生限流
     * 
     * @param context
     */
    void limit(InvocationContext context);

    /**
     * 执行错误
     * 
     * @param context
     * @param sourcePayload
     */
    void error(InvocationContext context, ResponsePayload sourcePayload);
}
