package conglin.clrpc.common.util.threadpool;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import conglin.clrpc.common.config.ConfigParser;

public class FixedThreadPool implements ThreadPool {

    @Override
    public ExecutorService getExecutorService() {
        int corePoolSize = ConfigParser.getInstance().getOrDefault("service.thread.pool.core-size", 5);
        int maximumPoolSize = ConfigParser.getInstance().getOrDefault("service.thread.pool.max-size", 10);
        long keepAliveTime = ConfigParser.getInstance().getOrDefault("service.thread.pool.max-size", 1000);
        int queues = ConfigParser.getInstance().getOrDefault("service.thread.pool.queues", 0);

        return new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.MILLISECONDS,
            queues == 0 ? new SynchronousQueue<Runnable>() :
                (queues < 0 ? new LinkedBlockingQueue<Runnable>() :
                     new LinkedBlockingQueue<Runnable>(queues))
            );
    }
    
}