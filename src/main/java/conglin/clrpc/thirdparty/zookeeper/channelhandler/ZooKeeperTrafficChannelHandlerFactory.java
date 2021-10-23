package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import conglin.clrpc.extension.traffic.channel.GlobalTrafficChannelHandler;
import conglin.clrpc.service.handler.factory.ChannelHandlerPhase;
import conglin.clrpc.service.handler.factory.DefaultChannelHandlerFactory;
import conglin.clrpc.service.handler.factory.DefaultOrderedChannelHandler;
import conglin.clrpc.service.handler.factory.OrderedChannelHandler;

import java.util.Collection;

public class ZooKeeperTrafficChannelHandlerFactory extends DefaultChannelHandlerFactory {

    @Override
    public Collection<OrderedChannelHandler> disorderlyHandlers() {
        Collection<OrderedChannelHandler> handlers = super.disorderlyHandlers();
        GlobalTrafficChannelHandler globalTrafficChannelHandler = new ZooKeeperGlobalTrafficChannelHandler();
        globalTrafficChannelHandler.setContext(getContext());
        globalTrafficChannelHandler.init();
        handlers.add(new DefaultOrderedChannelHandler(globalTrafficChannelHandler, ChannelHandlerPhase.BEFORE_CODEC));
        return handlers;
    }
}
