package conglin.clrpc.transfer.handler;

import io.netty.channel.ChannelHandler;

public interface ConsumerChannelInitializer extends ChannelHandler {
    BasicConsumerChannelHandler getBasicConsumerChannelHandler();
}