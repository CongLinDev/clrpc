package conglin.clrpc.executor;

import conglin.clrpc.executor.pipeline.AbstractChainExecutor;

public class BoundReverseExecutor extends AbstractChainExecutor {
    @Override
    public void inbound(Object object) {
        super.nextOutbound(object); // 反转
    }

    @Override
    public int order() {
        return Integer.MAX_VALUE - 1;
    }
}
