package conglin.clrpc.service.context.channel;

import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.future.FutureHolder;

final public class ConsumerChannelContext extends CommonChannelContext {

    public ConsumerChannelContext(ConsumerContext context) {
        super(context);
        futureHolder = context.getFuturesHolder();
    }


    private final FutureHolder<Long> futureHolder;
    /**
     * 获取Future持有者
     * 
     * @return
     */
    public FutureHolder<Long> futureHolder() {
        return futureHolder;
    }
}