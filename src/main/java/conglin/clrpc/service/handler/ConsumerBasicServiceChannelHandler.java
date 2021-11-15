package conglin.clrpc.service.handler;

import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.ResponsePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConsumerBasicServiceChannelHandler extends ConsumerAbstractServiceChannelHandler<Message> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBasicServiceChannelHandler.class);

    protected FutureHolder<Long> futureHolder;

    @Override
    boolean accept(Message msg) {
        return msg.payload() instanceof ResponsePayload;
    }

    @Override
    public void init() {
        futureHolder = getContext().getWith(RpcContextEnum.FUTURE_HOLDER);
    }

    @Override
    protected Object execute(Message msg) {
        Long messageId = msg.messageId();
        LOGGER.debug("Receive response (messageId={})", messageId);
        RpcFuture future = futureHolder.removeFuture(messageId);

        if (future != null) {
            future.done(msg.payload());
            return future;
        } else {
            LOGGER.error("Can not find binding future (messageId={})", msg.messageId());
        }
        return null;
    }
}