package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.context.ConsumerContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

abstract public class ConsumerAbstractServiceChannelHandler<T> extends SimpleChannelInboundHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAbstractServiceChannelHandler.class);

    protected final ConsumerContext context;

    public ConsumerAbstractServiceChannelHandler(ConsumerContext context) {
        this.context = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        context.getExecutorService().submit(() -> {
            ctx.fireChannelRead(execute(msg));
        });
    }

    /**
     * 执行具体方法
     * 
     * @param msg
     */
    abstract protected Object execute(T msg);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("ExceptionCaught : {}", cause);
        ctx.close();
    }
}