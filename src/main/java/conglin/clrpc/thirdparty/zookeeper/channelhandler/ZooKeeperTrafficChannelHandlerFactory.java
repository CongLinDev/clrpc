package conglin.clrpc.thirdparty.zookeeper.channelhandler;

import conglin.clrpc.extension.traffic.channel.GlobalTrafficChannelHandler;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.handler.factory.ChannelHandlerFactory;
import io.netty.channel.ChannelHandler;

import java.util.Collection;
import java.util.Collections;

public class ZooKeeperTrafficChannelHandlerFactory implements ChannelHandlerFactory, ContextAware {

    private RpcContext context;

    @Override
    public void setContext(RpcContext context) {
        this.context = context;
        init();
    }

    @Override
    public RpcContext getContext() {
        return context;
    }

    protected void init() {

    }

    @Override
    public Collection<ChannelHandler> beforeCodec() {
        GlobalTrafficChannelHandler globalTrafficChannelHandler = new ZooKeeperGlobalTrafficChannelHandler();
        globalTrafficChannelHandler.setContext(getContext());
        return Collections.singletonList(globalTrafficChannelHandler);
    }

}
