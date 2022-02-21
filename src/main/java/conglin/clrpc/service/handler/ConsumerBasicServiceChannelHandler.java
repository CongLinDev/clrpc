package conglin.clrpc.service.handler;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.InvocationFuture;
import conglin.clrpc.transport.message.Payload;
import conglin.clrpc.transport.message.ResponsePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerBasicServiceChannelHandler extends ConsumerAbstractServiceChannelHandler implements Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBasicServiceChannelHandler.class);

    protected FutureHolder<Long> futureHolder;

    @Override
    protected boolean accept(Payload payload) {
        return payload instanceof ResponsePayload;
    }

    @Override
    public void init() {
        futureHolder = getContext().getWith(ComponentContextEnum.FUTURE_HOLDER);
    }

    @Override
    protected Object execute(Long messageId, Payload payload) {
        LOGGER.debug("Receive response (messageId={})", messageId);
        InvocationFuture future = futureHolder.getFuture(messageId);

        if (future != null && future.isPending()) {
            ResponsePayload response = (ResponsePayload) payload;
            if (response.isError()) {
                future.failStrategy().error(payload);
            } else {
                future.done(payload);
            }
            return future;
        } else {
            LOGGER.error("Can not find binding future (messageId={})", messageId);
            return null;
        }
    }
}