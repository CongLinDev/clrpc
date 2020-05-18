package conglin.clrpc.extension.interceptor;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

/**
 * 用于拦截 {@link io.netty.channel.ChannelPipeline} 上的消息
 */
abstract public class Interceptor extends ChannelDuplexHandler {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (match(msg)) {
            inbound(msg);
        } else {
            super.channelRead(ctx, msg);
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (match(msg)) {

        } else {
            super.write(ctx, msg, promise);
        }
    }

    /**
     * 是否匹配
     * 
     * @param msg
     * @return
     */
    abstract protected boolean match(Object msg);

    /**
     * inbound的消息
     * 
     * @param msg
     */
    abstract protected void inbound(Object msg);

    /**
     * outbound的消息
     * 
     * @param msg
     */
    abstract protected void outbound(Object msg);
}