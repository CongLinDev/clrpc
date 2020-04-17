package conglin.clrpc.service.handler.traffic;

import conglin.clrpc.common.Calculatable;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.context.CommonContext;
import conglin.clrpc.service.handler.traffic.counter.CommonTrafficCounter;
import conglin.clrpc.service.handler.traffic.counter.TrafficCounter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public class ChannelTrafficHandler extends ChannelDuplexHandler implements Calculatable<TrafficCounter> {

    private final TrafficCounter counter;
    

    private final CommonContext context;

    public ChannelTrafficHandler(CommonContext context) {
        super();
        this.context = context;
        counter = new CommonTrafficCounter();
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
    protected String address(Role role, Channel channel) {
        if (role.isConsumer()) {
            return channel.localAddress().toString();
        } else if (role.isProvider()) {
            return channel.remoteAddress().toString();
        }
        return Role.UNKNOWN.toString();
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context.getSerivceLogger().put(address(context.role(), ctx.channel()), this);
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        context.getSerivceLogger().remove(address(context.role(), ctx.channel()));
        super.channelInactive(ctx);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        counter.submitRead(calculateSize(msg));
        super.channelRead(ctx, msg);
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        counter.submitWrite(calculateSize(msg));
        super.write(ctx, msg, promise);
    }

    /**
     * 计算对象大小
     * 
     * @param msg
     * @return
     */
    protected int calculateSize(Object msg) {
        if (msg instanceof ByteBuf) {
            return ((ByteBuf) msg).readableBytes();
        }
        if (msg instanceof ByteBufHolder) {
            return ((ByteBufHolder) msg).content().readableBytes();
        }
        return -1;
    }

    @Override
    public TrafficCounter calculate() {
        return counter;
    }
}