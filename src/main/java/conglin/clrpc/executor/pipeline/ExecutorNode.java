package conglin.clrpc.executor.pipeline;

public class ExecutorNode {
    
    private final ChainExecutor executor;
    ExecutorNode pre;
    ExecutorNode next;

    /**
     * 构造 ExecutorNode
     * 
     * @param executor
     */
    public ExecutorNode(ChainExecutor executor) {
        this.executor = executor;
        if (executor instanceof AbstractChainExecutor chainExecutor) {
            chainExecutor.bindExecutorNode(this);
        }
    }

    /**
     * order
     * 
     * @return
     */
    public int order() {
        return executor.order();
    }

    /**
     * next
     * 
     * @param object
     */
    public void nextInbound(Object object) {
        next.executor.inbound(object);
    }

    /**
     * next
     * 
     * @param object
     */
    public void nextOutbound(Object object) {
        pre.executor.outbound(object);
    }

    /**
     * @return the executor
     */
    public ChainExecutor executor() {
        return executor;
    }
}
