package conglin.clrpc.common.util.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import conglin.clrpc.common.config.ConfigParser;

public class FixedThreadPool implements ThreadPool {

    private int corePoolSize;
    private int maximumPoolSize;
    private long keepAliveTime;
    private int queues;

    public FixedThreadPool() {
        this(ConfigParser.getInstance().getOrDefault("service.thread.pool.core-size", 5),
            ConfigParser.getInstance().getOrDefault("service.thread.pool.max-size", 10),
            ConfigParser.getInstance().getOrDefault("service.thread.pool.keep-alive", 1000),
            ConfigParser.getInstance().getOrDefault("service.thread.pool.queues", 10));
    }

    public FixedThreadPool(int corePoolSize, int maximumPoolSize, long keepAliveTime, int queues) {
        this.corePoolSize = corePoolSize;
        this.maximumPoolSize = maximumPoolSize;
        this.keepAliveTime = keepAliveTime;
        this.queues = queues;
    }
    
    @Override
    public ExecutorService getExecutorService() {
        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
            queues == 0 ? new SynchronousQueue<Runnable>() :
                (queues < 0 ? new LinkedBlockingQueue<Runnable>() :
                     new LinkedBlockingQueue<Runnable>(queues))
            );
    }
}