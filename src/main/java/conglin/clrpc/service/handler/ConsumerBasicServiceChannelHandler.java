package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transport.message.BasicResponse;

public class ConsumerBasicServiceChannelHandler extends ConsumerAbstractServiceChannelHandler<BasicResponse> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerBasicServiceChannelHandler.class);

    protected final FuturesHolder<Long> futuresHolder;

    public ConsumerBasicServiceChannelHandler(ConsumerContext context) {
        super(context);
        this.futuresHolder = context.getFuturesHolder();
    }

    @Override
    protected Object execute(BasicResponse msg) {
        Long messageId = msg.getMessageId();
        LOGGER.debug("Receive response (messageId={})", messageId);
        RpcFuture future = futuresHolder.removeFuture(messageId);

        if (future != null) {
            future.done(msg);
            BasicFuture f = (BasicFuture) future;
            return new Pair<>(f.request(), msg);
        }

        return msg;
    }
}