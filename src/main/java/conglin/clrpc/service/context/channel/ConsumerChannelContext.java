package conglin.clrpc.service.context.channel;

import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.future.FutureHolder;

final public class ConsumerChannelContext extends CommonChannelContext {

    public ConsumerChannelContext(RpcContext context) {
        super(context);
        futureHolder = context.getWith(RpcContextEnum.FUTURE_HOLDER);
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