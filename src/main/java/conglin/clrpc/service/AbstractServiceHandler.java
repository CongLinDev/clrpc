package conglin.clrpc.service;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.threadpool.FixedThreadPool;
import conglin.clrpc.common.util.threadpool.ThreadPool;

abstract public class AbstractServiceHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractServiceHandler.class);
    // 业务线程池
    private final ExecutorService businessTheardExecutorService;

    public AbstractServiceHandler(){
        String threadpoolName = ConfigParser.getInstance().getOrDefault("service.thread.pool.class",
            "conglin.clrpc.common.util.threadpool.FixedThreadPool");

        ExecutorService executorService = null;
        try {
            ThreadPool threadPool = (ThreadPool) Class.forName(threadpoolName)
                    .getConstructor().newInstance();
            executorService = threadPool.getExecutorService();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.common.util.threadpool.FixedThreadPool' rather than "
                    + threadpoolName);
        }finally{
            // 如果类名错误，则默认加载 {link conglin.clrpc.common.util.threadpool.FixedThreadPool}
            businessTheardExecutorService = 
                (executorService == null) 
                    ? (new FixedThreadPool()).getExecutorService()
                    : executorService;
        }
    }
    
    /**
     * 提交一个 {link java.util.concurrent.Callable} 任务
     * 
     * @param <T>
     * @param task
     * @return
     */
    public <T> Future<T> submit(Callable<T> task) {
        return businessTheardExecutorService.submit(task);
    }

    /**
     * 提交一个 {link java.lang.Runnable} 任务
     * 
     * @param task
     * @return
     */
    public Future<?> submit(Runnable task) {
        return businessTheardExecutorService.submit(task);
    }

    public void stop(){
        if(businessTheardExecutorService != null)
            businessTheardExecutorService.shutdown();
    }
}