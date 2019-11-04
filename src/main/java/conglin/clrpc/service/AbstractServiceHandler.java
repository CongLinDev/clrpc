package conglin.clrpc.service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.security.auth.DestroyFailedException;
import javax.security.auth.Destroyable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;

abstract public class AbstractServiceHandler implements Destroyable {
    private static final Logger log = LoggerFactory.getLogger(AbstractServiceHandler.class);
    // 业务线程池
    private final ExecutorService businessTheardExecutorService;

    public AbstractServiceHandler(PropertyConfigurer configurer){
        businessTheardExecutorService = threadPool(configurer);
    }

    /**
     * 根据配置文件返回一个线程池
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
     * @param corePoolSize 核心线程数
     * @param maximumPoolSize 最大线程数
     * @param keepAliveTime 线程存活时间
     * @param queues 任务队列数
     * @return
     */
    public ExecutorService threadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queues) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
            queues == 0 ? new SynchronousQueue<Runnable>() :
                (queues < 0 ? new LinkedBlockingQueue<Runnable>() :
                    new LinkedBlockingQueue<Runnable>(queues)),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
    }

    @Override
    public void destroy() throws DestroyFailedException {
        businessTheardExecutorService.shutdown();
        log.debug("Theard Executor shuts down.");
    }

    @Override
    public boolean isDestroyed() {
        return businessTheardExecutorService.isShutdown();
    }


    /**
     * 返回绑定的线程池
     * @return
     */
    public ExecutorService getExecutorService() {
        return businessTheardExecutorService;
    }
}