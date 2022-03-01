package conglin.clrpc.service.handler;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.service.context.InvocationContextHolder;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerBasicServiceChannelHandler extends ConsumerAbstractServiceChannelHandler implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBasicServiceChannelHandler.class);

    protected InvocationContextHolder<Long> contextHolder;

    @Override
    protected boolean accept(Payload payload) {
        return payload instanceof ResponsePayload;
    }

    @Override
    public void init() {
        contextHolder = getContext().getWith(ComponentContextEnum.INVOCATION_CONTEXT_HOLDER);
    }

    @Override
    protected Object execute(Long messageId, Payload payload) {
        LOGGER.debug("Receive response (messageId={})", messageId);
        InvocationContext invocationContext = contextHolder.get(messageId);
        if (invocationContext == null) {
            LOGGER.error("Can not find binding invocationContext (messageId={})", messageId);
            return null;
        }

        InvocationFuture future = invocationContext.getFuture();
        if (future.isPending()) {
            ResponsePayload response = (ResponsePayload) payload;
            if (response.isError()) {
                invocationContext.getFailStrategy().error(invocationContext, payload);
            } else {
                invocationContext.setResponse(response);
            }
            return invocationContext;
        } else {
            LOGGER.error("Can not find binding invocationContext (messageId={})", messageId);
            return invocationContext;
        }
    }
}