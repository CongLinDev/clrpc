package conglin.clrpc.executor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.executor.pipeline.CommonChainExecutor;
import conglin.clrpc.invocation.InvocationContext;
import conglin.clrpc.invocation.message.Message;
import conglin.clrpc.lifecycle.ComponentContextEnum;
import conglin.clrpc.lifecycle.Initializable;
import conglin.clrpc.service.router.NoAvailableServiceInstancesException;
import conglin.clrpc.service.router.Router;
import conglin.clrpc.service.router.RouterCondition;
import conglin.clrpc.service.router.RouterResult;

public class NetworkClientExecutor extends CommonChainExecutor implements Initializable {
    private static final Logger LOGGER = LoggerFactory.getLogger(NetworkClientExecutor.class);
    protected Router router;

    @Override
    public void init() {
        this.router = getContext().getWith(ComponentContextEnum.ROUTER);
    }

    @Override
    public int order() {
        return 1;
    }

    @Override
    public void outbound(Object object) {
        if (object instanceof InvocationContext invocationContext) {
            execute(invocationContext);
        } else {
            super.nextOutbound(object);
        }
    }

    protected void execute(InvocationContext invocationContext) {
        RouterCondition condition = new RouterCondition();
        condition.setServiceName(invocationContext.getRequest().serviceName());
        condition.setRandom(invocationContext.getExecuteTimes() ^ invocationContext.getIdentifier().intValue());
        condition.setInstanceCondition(invocationContext.getChoosedInstanceCondition());
        try {
            RouterResult routerResult = router.choose(condition);
            if (invocationContext.getChoosedInstancePostProcessor() != null) {
                invocationContext.getChoosedInstancePostProcessor().accept(routerResult.getInstance());
            }
            invocationContext.increaseExecuteTimes();
            routerResult.execute(new Message(invocationContext.getIdentifier(), invocationContext.getRequest()));
            LOGGER.debug("Execute request for messageId={}", invocationContext.getIdentifier());
        } catch (NoAvailableServiceInstancesException e) {
            invocationContext.getFailStrategy().noTarget(invocationContext, e);
        }
    }

}
