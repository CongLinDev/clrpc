package conglin.clrpc.executor.pipeline;

import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ComponentContextAware;

public class CommonChainExecutor extends AbstractChainExecutor implements ComponentContextAware {

    private ComponentContext context;

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public ComponentContext getContext() {
        return context;
    }
}
