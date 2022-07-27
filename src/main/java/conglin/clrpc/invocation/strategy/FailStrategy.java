package conglin.clrpc.invocation.strategy;

import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.message.Payload;
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
     * @return 是否重试
     */
    boolean noTarget(InvocationContext context, NoAvailableServiceInstancesException exception);

    /**
     * 发生超时
     * 
     * @param context
     * @return 是否重试
     */
    boolean timeout(InvocationContext context);

    /**
     * 发生限流
     * 
     * @param context
     * @return 是否重试
     */
    boolean limit(InvocationContext context);

    /**
     * 执行错误
     * 
     * @param context
     * @param sourcePayload
     * @return 是否重试
     */
    boolean error(InvocationContext context, Payload sourcePayload);
}
