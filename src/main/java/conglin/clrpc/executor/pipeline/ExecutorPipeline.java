package conglin.clrpc.executor.pipeline;

public interface ExecutorPipeline {

    /**
     * 注册 {@link #Executor}
     * 
     * @param executor
     */
    void register(ChainExecutor executor);

    /**
     * 将数据发送给第一个 {@link #Executor}
     * 
     * @param object
     */
    void inbound(Object object);

    /**
     * 将数据发送给最后一个 {@link #Executor}
     * 
     * @param object
     */
    void outbound(Object object);
}
