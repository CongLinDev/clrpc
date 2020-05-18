package conglin.clrpc.extension.interceptor;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

abstract public class TypeInterceptor extends Interceptor {

    private final Class<?> interceptedType;

    public TypeInterceptor(Class<?> interceptedType) {
        super();
        this.interceptedType = interceptedType;
    }

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

    @Override
    protected boolean match(Object msg) {
        return interceptedType.isInstance(msg);
    }
}