package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.context.ConsumerContext;
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
        Long requestId = msg.getRequestId();
        LOGGER.debug("Receive response responseId=" + requestId);

        RpcFuture future = futuresHolder.removeFuture(requestId);

        if (future != null) {
            future.done(msg);
        }
        return msg;
    }

}