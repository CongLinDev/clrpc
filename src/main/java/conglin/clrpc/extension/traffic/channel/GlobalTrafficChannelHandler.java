package conglin.clrpc.extension.traffic.channel;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.registry.ServiceLogger;
import conglin.clrpc.definition.role.Role;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
abstract public class GlobalTrafficChannelHandler extends ChannelInboundHandlerAdapter implements ComponentContextAware, Initializable {

    protected ServiceLogger serviceLogger;

    private ComponentContext context;

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    /**
     * 为了服务端和客户端保证 {@link Channel} 的唯一性
     * 
     * 使用网络地址作为标识符，而不是使用 {@link io.netty.channel.ChannelId}
     * 
     * @param role
     * @param channel
     * @return
     */
    protected String id(Role role, Channel channel) {
        if (role.isConsumer()) {
            return role.toString() + channel.localAddress().toString();
        } else if (role.isProvider()) {
            return role.toString() + channel.remoteAddress().toString();
        }
        return role.toString();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        TrafficChannelHandler handler = new TrafficChannelHandler();
        serviceLogger.put(id(getContext().getWith(ComponentContextEnum.ROLE), ctx.channel()), handler);
        ctx.pipeline().addFirst(handler);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        serviceLogger.remove(id(getContext().getWith(ComponentContextEnum.ROLE), ctx.channel()));
        super.channelInactive(ctx);
    }
}