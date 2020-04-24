package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.context.channel.ConsumerChannelContext;
import conglin.clrpc.service.future.FutureHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicResponse;

public class ConsumerBasicServiceChannelHandler extends ConsumerAbstractServiceChannelHandler<BasicResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBasicServiceChannelHandler.class);

    protected final FutureHolder<Long> futureHolder;

    public ConsumerBasicServiceChannelHandler(ConsumerChannelContext context) {
        super(context);
        this.futureHolder = context.futureHolder();
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