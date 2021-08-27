package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import conglin.clrpc.extension.traffic.channel.GlobalTrafficChannelHandler;
import conglin.clrpc.service.handler.factory.DefaultChannelHandlerFactory;
import io.netty.channel.ChannelHandler;

import java.util.Collection;
import java.util.Collections;

public class ZooKeeperTrafficChannelHandlerFactory extends DefaultChannelHandlerFactory {

    @Override
    protected Collection<ChannelHandler> beforeCodec() {
        GlobalTrafficChannelHandler globalTrafficChannelHandler = new ZooKeeperGlobalTrafficChannelHandler();
        globalTrafficChannelHandler.setContext(getContext());
        return Collections.singletonList(globalTrafficChannelHandler);
    }

}
