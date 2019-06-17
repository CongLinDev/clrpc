package conglin.clrpc.transfer.net.handler;

import io.netty.channel.ChannelHandler;

public interface ClientChannelInitializer extends ChannelHandler {
    BasicClientChannelHandler getBasicClientChannelHandler();
}