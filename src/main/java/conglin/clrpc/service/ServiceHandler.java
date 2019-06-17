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

public abstract class ServiceHandler {
    private static final Logger log = LoggerFactory.getLogger(ServiceHandler.class);
    // 业务线程池
    private ExecutorService businessTheardExecutorService;

    /**
     * 获取一个业务线程池
     * 
     * @return
     */
    private ExecutorService getBusinessTheardExecutorService() {
        if (businessTheardExecutorService == null) {
            String threadpoolName = ConfigParser.getInstance().getOrDefault("service.thread.pool.class",
                    "conglin.clrpc.common.util.threadpool.FixedThreadPool");
            synchronized (ServerServiceHandler.class) {
                if (businessTheardExecutorService == null) {
                    try {
                        ThreadPool threadPool = (ThreadPool) Class.forName(threadpoolName)
                                .getConstructor().newInstance();
                        businessTheardExecutorService = threadPool.getExecutorService();
                    } catch (InstantiationException | IllegalAccessException | IllegalArgumentException
                            | InvocationTargetException | NoSuchMethodException | SecurityException
                            | ClassNotFoundException e) {

                        log.error("{} .Loading conglin.clrpc.common.util.threadpool.FixedThreadPool rather than "
                                + threadpoolName, e);
                        // 如果类名错误，则默认加载 {link conglin.clrpc.common.util.threadpool.FixedThreadPool}
                        businessTheardExecutorService = (new FixedThreadPool()).getExecutorService();
                    }
                }
            }
        }

        return businessTheardExecutorService;
    }

    /**
     * 提交一个 {link java.util.concurrent.Callable} 任务
     * 
     * @param <T>
     * @param task
     * @return
     */
    public <T> Future<T> submit(Callable<T> task) {
        ExecutorService executorService = getBusinessTheardExecutorService();
        return executorService.submit(task);
    }

    /**
     * 提交一个 {link java.lang.Runnable} 任务
     * 
     * @param task
     * @return
     */
    public Future<?> submit(Runnable task) {
        ExecutorService executorService = getBusinessTheardExecutorService();
        return executorService.submit(task);
    }

    public void stop(){
        if(businessTheardExecutorService != null)
            businessTheardExecutorService.shutdown();
    }
}