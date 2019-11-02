package conglin.clrpc.service;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;

abstract public class AbstractServiceHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractServiceHandler.class);
    // 业务线程池
    private final ExecutorService businessTheardExecutorService;

    public AbstractServiceHandler(PropertyConfigurer configurer){
        businessTheardExecutorService = threadPool(configurer);
    }

    public ExecutorService threadPool(PropertyConfigurer configurer) {
        return threadPool(configurer.getOrDefault("service.thread.pool.core-size", 5),
            configurer.getOrDefault("service.thread.pool.max-size", 10),
            configurer.getOrDefault("service.thread.pool.keep-alive", 1000),
            configurer.getOrDefault("service.thread.pool.queues", 10));
    }
    
    public ExecutorService threadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queues) {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
            queues == 0 ? new SynchronousQueue<Runnable>() :
                (queues < 0 ? new LinkedBlockingQueue<Runnable>() :
                    new LinkedBlockingQueue<Runnable>(queues)),
                new ThreadPoolExecutor.CallerRunsPolicy()
            );
    }
    
    /**
     * 提交一个 {@link java.util.concurrent.Callable} 任务
     * 
     * @param <T>
     * @param task
     * @return
     */
    public <T> Future<T> submit(Callable<T> task) {
        return businessTheardExecutorService.submit(task);
    }

    /**
     * 提交一个 {@link java.lang.Runnable} 任务
     * 
     * @param task
     * @return
     */
    public Future<?> submit(Runnable task) {
        return businessTheardExecutorService.submit(task);
    }

    /**
     * 提交一个 {@link java.lang.Runnable} 任务
     * 
     * @param task
     * @return 
     */
    public void execute(Runnable task){
        businessTheardExecutorService.execute(task);
    }

    /**
     * 销毁业务线程池
     */
    protected void destory(){
        if(businessTheardExecutorService != null)
            businessTheardExecutorService.shutdown();
        log.info("Theard Executor shuts down.");
    }


    public ExecutorService getExecutorService() {
        return businessTheardExecutorService;
    }
}