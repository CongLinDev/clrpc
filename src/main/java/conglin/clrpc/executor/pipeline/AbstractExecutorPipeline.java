package conglin.clrpc.executor.pipeline;

import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract public class AbstractExecutorPipeline implements ExecutorPipeline {

    private final ExecutorNode head;
    private final ExecutorNode tail;

    public AbstractExecutorPipeline() {
        head = new ExecutorNode(new HeadExecutor());
        tail = new ExecutorNode(new TailExecutor());
        head.next = tail;
        tail.pre = head;
    }

    @Override
    public void register(ChainExecutor executor) {
        ExecutorNode current = head;
        while (current != tail && current.order() <= executor.order()) {
            current = current.next;
        }
        ExecutorNode pre = current.pre;
        ExecutorNode node = new ExecutorNode(executor);
        pre.next = node;
        node.pre = pre;
        node.next = current;
        current.pre = node;
    }

    @Override
    public void unregister(String name) {
        ExecutorNode current = head.next;
        while (current != tail) {
            if (current.executor().name().equals(name)) {
                ExecutorNode pre = current.pre;
                ExecutorNode next = current.next;
                pre.next = next;
                next.pre = pre;
                doUnregister(current.executor());
                current = pre;
            }
            current = current.next;
        }
    }

    /**
     * Unregister action
     * 
     * @param executor
     */
    protected void doUnregister(ChainExecutor executor) {

    }

    @Override
    public void inbound(Object object) {
        head.executor().inbound(object);
    }

    @Override
    public void outbound(Object object) {
        tail.executor().outbound(object);
    }

    /**
     * for each
     * 
     * @param consumer
     */
    public void forEach(Consumer<ChainExecutor> consumer) {
        ExecutorNode current = head;
        while (current != null) {
            consumer.accept(current.executor());
            current = current.next;
        }
    }

    protected static class HeadExecutor extends AbstractChainExecutor {
        private static final Logger LOGGER = LoggerFactory.getLogger(HeadExecutor.class);

        @Override
        public void outbound(Object object) {
            if (object == null)
                return;
            LOGGER.warn("There is an unexpected object {}", object);
        }

        @Override
        public int order() {
            return Integer.MIN_VALUE;
        }

        @Override
        public String name() {
            return "HeadExecutor";
        }
    }

    protected static class TailExecutor extends AbstractChainExecutor {

        private static final Logger LOGGER = LoggerFactory.getLogger(TailExecutor.class);

        @Override
        public void inbound(Object object) {
            if (object == null)
                return;
            LOGGER.warn("There is an unexpected object {}", object);
        }

        @Override
        public int order() {
            return Integer.MAX_VALUE;
        }

        @Override
        public String name() {
            return "TailExecutor";
        }
    }

}
