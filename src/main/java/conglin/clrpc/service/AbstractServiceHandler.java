package conglin.clrpc.service;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.threadpool.CustomizedThreadPool;
import conglin.clrpc.common.util.threadpool.ThreadPool;

abstract public class AbstractServiceHandler {
    private static final Logger log = LoggerFactory.getLogger(AbstractServiceHandler.class);
    // 业务线程池
    private final ExecutorService businessTheardExecutorService;

    /**
     * 你可以创建一个实现了 {@link conglin.clrpc.common.util.threadpool.ThreadPool}
     * 或是继承了  {@link conglin.clrpc.common.util.threadpool.CustomizedThreadPool}
     * 的线程池创建器来创建一个你想要的业务线程池，只需要在配置文件中写出其完整类名即可
     * 需要注意的是，你创建的线程池必须有一个无参的构造函数
     */
    public AbstractServiceHandler(){
        String threadpoolName = ConfigParser.getOrDefault("service.thread.pool.class",
            "conglin.clrpc.common.util.threadpool.CustomizedThreadPool");

        ExecutorService executorService = null;
        try {
            ThreadPool threadPool = (ThreadPool) Class.forName(threadpoolName)
                    .getConstructor().newInstance();
            executorService = threadPool.getExecutorService();
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | NoSuchMethodException | SecurityException
                | ClassNotFoundException e) {
            log.warn(e.getMessage() + ". Loading 'conglin.clrpc.common.util.threadpool.CustomizedThreadPool' rather than "
                    + threadpoolName);
        }finally{
            // 如果类名错误，则默认加载 {@link conglin.clrpc.common.util.threadpool.FixedThreadPool}
            businessTheardExecutorService = 
                (executorService == null) 
                    ? (new CustomizedThreadPool()).getExecutorService()
                    : executorService;
        }
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

    public void stop(){
        if(businessTheardExecutorService != null)
            businessTheardExecutorService.shutdown();
    }
}