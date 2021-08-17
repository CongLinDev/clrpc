package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.context.channel.ConsumerChannelContext;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

abstract public class ConsumerAbstractServiceChannelHandler<T> extends SimpleChannelInboundHandler<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAbstractServiceChannelHandler.class);

    protected final ConsumerChannelContext context;

    public ConsumerAbstractServiceChannelHandler(ConsumerChannelContext context) {
        this.context = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        context.executorService().submit(() -> {
            Object result = execute(msg);
            if(result != null)
                ctx.fireChannelRead(result);
        });
    }

    /**
     * 执行具体方法
     * 
     * @param msg
     * @return 执行后的结果
     */
    abstract protected Object execute(T msg);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("ExceptionCaught : {}", cause.getMessage());
        ctx.close();
    }
}