package conglin.clrpc.executor.pipeline;

import conglin.clrpc.lifecycle.ComponentContext;
import conglin.clrpc.lifecycle.ComponentContextAware;
import conglin.clrpc.lifecycle.Destroyable;
import conglin.clrpc.lifecycle.Initializable;
import conglin.clrpc.lifecycle.ObjectLifecycleUtils;

public class CommonExecutorPipeline extends AbstractExecutorPipeline implements Initializable, ComponentContextAware, Destroyable {
    private ComponentContext componentContext;

    @Override
    public void setContext(ComponentContext context) {
        this.componentContext = context;
    }

    @Override
    public ComponentContext getContext() {
        return componentContext;
    }

    @Override
    public void init() {
        forEach(executor -> ObjectLifecycleUtils.assemble(executor, getContext()));
    }

    @Override
    public void destroy() {
        forEach(ObjectLifecycleUtils::destroy);
    }

    @Override
    protected void doUnregister(ChainExecutor executor) {
        ObjectLifecycleUtils.destroy(executor);
    }
    
}
