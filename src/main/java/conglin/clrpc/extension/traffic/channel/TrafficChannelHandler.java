package conglin.clrpc.extension.traffic.channel;

import conglin.clrpc.common.Calculatable;
import conglin.clrpc.extension.traffic.counter.CommonTrafficCounter;
import conglin.clrpc.extension.traffic.counter.TrafficCounter;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufHolder;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

class TrafficChannelHandler extends ChannelDuplexHandler implements Calculatable<TrafficCounter> {

    private final TrafficCounter counter;

    public TrafficChannelHandler() {
        super();
        counter = new CommonTrafficCounter();
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