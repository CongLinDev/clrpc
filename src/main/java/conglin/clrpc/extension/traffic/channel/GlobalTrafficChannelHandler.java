package conglin.clrpc.extension.traffic.channel;

import conglin.clrpc.common.object.UrlScheme;
import conglin.clrpc.common.registry.ServiceLogger;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.context.channel.CommonChannelContext;
import conglin.clrpc.thirdparty.zookeeper.registry.ZooKeeperServiceLogger;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

@Sharable
public class GlobalTrafficChannelHandler extends ChannelInboundHandlerAdapter {

    private final ServiceLogger serviceLogger;

    private final CommonChannelContext context;

    public GlobalTrafficChannelHandler(CommonChannelContext context) {
        this.context = context;
        String urlString = context.propertyConfigurer().get("extension.logger", String.class);
        serviceLogger = new ZooKeeperServiceLogger(new UrlScheme(urlString));
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
        serviceLogger.put(id(context.role(), ctx.channel()), handler);
        ctx.pipeline().addFirst(handler);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        serviceLogger.remove(id(context.role(), ctx.channel()));
        super.channelInactive(ctx);
    }
}