package conglin.clrpc.service.handler;

import conglin.clrpc.service.context.RpcContextEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicResponse;

public class ConsumerBasicServiceChannelHandler extends ConsumerAbstractServiceChannelHandler<BasicResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBasicServiceChannelHandler.class);

    protected FutureHolder<Long> futureHolder;

    @Override
    public void init() {
        futureHolder = getContext().getWith(RpcContextEnum.FUTURE_HOLDER);
    }

    @Override
    protected Object execute(BasicResponse msg) {
        Long messageId = msg.messageId();
        LOGGER.debug("Receive response (messageId={})", messageId);
        RpcFuture future = futureHolder.removeFuture(messageId);

        if (future != null) {
            future.done(msg);
            return future;
        }
        return null;
    }
}