package conglin.clrpc.service.executor;

import java.util.concurrent.ExecutorService;

import conglin.clrpc.common.Destroyable;

public interface ServiceExecutor<T> extends Destroyable {
    /**
     * 获取一个异步执行器
     * @return
     */
    ExecutorService getExecutorService();

    /**
     * 使用异步执行器执行服务
     * @param t
     */
    void execute(T t);
}