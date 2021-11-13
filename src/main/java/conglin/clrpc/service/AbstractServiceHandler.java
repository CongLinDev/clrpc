package conglin.clrpc.service;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.*;

abstract public class AbstractServiceHandler implements Destroyable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceHandler.class);
    // 业务线程池
    private final ExecutorService businessThreadExecutorService;

    private RpcContext context;

    /**
     * 构造 {@link AbstractServiceHandler}
     * 
     * 本质上是构造线程池
     * 
     * @param properties 配置
     */
    public AbstractServiceHandler(Properties properties) {
        businessThreadExecutorService = threadPool(properties);
    }

    /**
     * 根据配置文件返回一个线程池
     * 
     * @param properties 配置
     * @return 线程池
     */
    public ExecutorService threadPool(Properties properties) {
        return threadPool(Integer.parseInt(properties.getProperty("service.thread-pool.core-size", "5")),
                Integer.parseInt(properties.getProperty("service.thread-pool.max-size", "10")),
                Integer.parseInt(properties.getProperty("service.thread-pool.keep-alive", "1000")),
                Integer.parseInt(properties.getProperty("service.thread-pool.queue", "10")));
    }

    /**
     * 根据给定数据返回相应的线程池
     * 
     * @param corePoolSize    核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime   线程存活时间
     * @param queues          任务队列数
     * @return
     */
    public ExecutorService threadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queues) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS, queues == 0
                ? new SynchronousQueue<Runnable>()
                : (queues < 0 ? new LinkedBlockingQueue<Runnable>() : new LinkedBlockingQueue<Runnable>(queues)),
                REJECT_HANDLER);
    }

    /**
     * 线程池拒绝策略
     * 
     * 当任务因线程池满了被拒绝后，首先在指定时间内再次尝试加入线程池。若失败，则创建临时线程运行该任务。若临时线程创建失败，则抛出异常。
     */
    private static final RejectedExecutionHandler REJECT_HANDLER = (runnable, executor) -> {

        boolean offered = false;
        try {
            // 再次尝试将其加入线程池
            offered = executor.getQueue().offer(runnable, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }

        if (!offered) {
            LOGGER.debug("Read task failed");
            try {
                // 直接创建临时线程运行任务
                new Thread(runnable, "Temporary task executor").start();
            } catch (Throwable throwable) {
                throw new RejectedExecutionException("Failed to start a new thread ", throwable);
            }
        }
    };

    @Override
    public void destroy() throws DestroyFailedException {
        if (businessThreadExecutorService == null)
            return;
        businessThreadExecutorService.shutdown();
        LOGGER.debug("Thread Executor shuts down.");
    }

    @Override
    public boolean isDestroyed() {
        if (businessThreadExecutorService == null)
            return true;
        return businessThreadExecutorService.isShutdown();
    }

    /**
     * 返回绑定的线程池
     * 
     * @return
     */
    public ExecutorService getExecutorService() {
        return businessThreadExecutorService;
    }

    /**
     * 启动
     *
     * @param context
     */
    public void start(RpcContext context) {
        this.context = context;
        context.put(RpcContextEnum.EXECUTOR_SERVICE, getExecutorService());
    }

    /**
     * 停止
     */
    abstract public void stop();

    /**
     * 获取 context
     *
     * @return
     */
    protected RpcContext context() {
        return context;
    }
}