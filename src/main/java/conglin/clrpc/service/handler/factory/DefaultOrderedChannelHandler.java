package conglin.clrpc.service.handler.factory;

import io.netty.channel.ChannelHandler;

public class DefaultOrderedChannelHandler implements OrderedChannelHandler {

    private final ChannelHandler channelHandler;
    private final ChannelHandlerPhase phase;
    private final int order;

    public DefaultOrderedChannelHandler(ChannelHandler channelHandler, ChannelHandlerPhase phase, int order) {
        this.channelHandler = channelHandler;
        this.phase = phase;
        this.order = order;
    }

    public DefaultOrderedChannelHandler(ChannelHandler channelHandler, ChannelHandlerPhase phase) {
        this(channelHandler, phase, 0);
    }

    @Override
    public ChannelHandler channelHandler() {
        return channelHandler;
    }

    @Override
    public int order() {
        return order;
    }

    @Override
    public ChannelHandlerPhase phase() {
        return phase;
    }
}
