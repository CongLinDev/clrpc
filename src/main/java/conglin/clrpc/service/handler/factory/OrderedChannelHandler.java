package conglin.clrpc.service.handler.factory;

import io.netty.channel.ChannelHandler;

public interface OrderedChannelHandler extends Comparable<OrderedChannelHandler> {

    /**
     * handler
     *
     * @return
     */
    ChannelHandler channelHandler();

    /**
     * 顺序 越小越在前面
     *
     * @return
     */
    default int order() {
        return 0;
    }

    /**
     * 作用时机
     *
     * @return
     */
    ChannelHandlerPhase phase();

    @Override
    default int compareTo(OrderedChannelHandler o) {
        int state = phase().compareTo(o.phase());
        if (state != 0) return state;
        return order() - o.order();
    }
}
