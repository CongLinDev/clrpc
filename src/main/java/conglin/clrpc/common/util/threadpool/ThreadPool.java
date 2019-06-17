package conglin.clrpc.common.util.threadpool;

import java.util.concurrent.ExecutorService;

public interface ThreadPool {
    /**
     * 返回一个线程池
     * @return
     */
    ExecutorService getExecutorService();
}