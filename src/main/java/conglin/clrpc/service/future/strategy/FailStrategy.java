package conglin.clrpc.service.future.strategy;

import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.router.NoAvailableServiceInstancesException;

/**
 * 失败策略
 */
public interface FailStrategy {
    /**
     * 没有目标
     * 
     * @param exception
     * @return 是否重试
     */
    boolean noTarget(NoAvailableServiceInstancesException exception);

    /**
     * 是否超时
     * 
     * @return
     */
    boolean isTimeout();

    /**
     * 发生超时
     * 
     * @return 是否重试
     */
    boolean timeout();

    /**
     * 发生限流
     * 
     * @return 是否重试
     */
    boolean limit();

    /**
     * 执行错误
     * 
     * @param sourcePayload
     * @return 是否重试
     */
    boolean error(Payload sourcePayload);
}
