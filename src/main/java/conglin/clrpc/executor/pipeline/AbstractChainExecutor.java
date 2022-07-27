package conglin.clrpc.executor.pipeline;

abstract public class AbstractChainExecutor implements ChainExecutor {
    private ExecutorNode node;

    @Override
    public void inbound(Object object) {
        nextInbound(object);
    }

    @Override
    public void outbound(Object object) {
        nextOutbound(object);
    }

    @Override
    public void nextInbound(Object object) {
        node.nextInbound(object);
    }

    @Override
    public void nextOutbound(Object object) {
        node.nextOutbound(object);
    }

    /**
     * 绑定 node
     * 
     * @param node
     */
    void bindExecutorNode(ExecutorNode node) {
        this.node = node;
    }
}
