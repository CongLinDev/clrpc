package conglin.clrpc.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Destroyable;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.exception.DestroyFailedException;
import conglin.clrpc.service.context.CommonContext;
import conglin.clrpc.zookeeper.registry.ZooKeeperServiceLogger;

abstract public class AbstractServiceHandler implements Destroyable {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractServiceHandler.class);
    // 业务线程池
    private final ExecutorService businessTheardExecutorService;

    public AbstractServiceHandler(PropertyConfigurer configurer, boolean enable) {
        businessTheardExecutorService = enable ? threadPool(configurer) : null;
    }

    public AbstractServiceHandler(PropertyConfigurer configurer) {
        this(configurer, true);
    }

    /**
     * 根据配置文件返回一个线程池
     * 
     * @param configurer
     * @return
     */
    public ExecutorService threadPool(PropertyConfigurer configurer) {
        return threadPool(configurer.getOrDefault("service.thread-pool.core-size", 5),
                configurer.getOrDefault("service.thread-pool.max-size", 10),
                configurer.getOrDefault("service.thread-pool.keep-alive", 1000),
                configurer.getOrDefault("service.thread-pool.queues", 10));
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
    private static RejectedExecutionHandler REJECT_HANDLER = (runnable, executor) -> {

        boolean offered = false;
        try {
            // 再次尝试将其加入线程池
            offered = executor.getQueue().offer(runnable, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error(e.getMessage());
        }

        if (!offered) {
            LOGGER.debug("Readd task failed");
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
        if (businessTheardExecutorService == null)
            return;
        businessTheardExecutorService.shutdown();
        LOGGER.debug("Theard Executor shuts down.");
    }

    @Override
    public boolean isDestroyed() {
        if (businessTheardExecutorService == null)
            return true;
        return businessTheardExecutorService.isShutdown();
    }

    /**
     * 返回绑定的线程池
     * 
     * @return
     */
    public ExecutorService getExecutorService() {
        return businessTheardExecutorService;
    }

    /**
     * 初始化上下文
     * 
     * @param context
     */
    protected void initContext(CommonContext context) {
        context.setExecutorService(getExecutorService());
        context.setServiceLogger(new ZooKeeperServiceLogger(context.role(), context.getPropertyConfigurer()));
    }
}