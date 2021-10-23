package conglin.clrpc.service.handler.factory;

import io.netty.channel.ChannelHandler;

import java.util.Collection;
import java.util.stream.Collectors;

public interface OrderedChannelHandlerFactory extends ChannelHandlerFactory {

    /**
     * 无序的handler集合
     *
     * @return
     */
    Collection<OrderedChannelHandler> disorderlyHandlers();

    @Override
    default Collection<ChannelHandler> handlers() {
        return disorderlyHandlers()
                .stream()
                .sorted(OrderedChannelHandler::compareTo)
                .map(OrderedChannelHandler::channelHandler)
                .collect(Collectors.toList());
    }
}
