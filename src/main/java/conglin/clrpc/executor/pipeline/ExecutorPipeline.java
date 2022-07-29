package conglin.clrpc.executor.pipeline;

/**
 * inbound     head -> ... -> ... -> tail
 * 
 * 
 * outbound    head <- ... <- ... <- tail
 * 
 * 
 * order      min-int     ...      max-int
 */
public interface ExecutorPipeline {

    /**
     * 注册 {@link #ChainExecutor}
     * 
     * @param executor
     */
    void register(ChainExecutor executor);

    /**
     * 取消注册 {@link #ChainExecutor}
     * 
     * @param name
     */
    void unregister(String name);

    /**
     * 将数据发送给第一个 {@link #ChainExecutor}
     * 
     * @param object
     */
    void inbound(Object object);

    /**
     * 将数据发送给最后一个 {@link #ChainExecutor}
     * 
     * @param object
     */
    void outbound(Object object);
}
