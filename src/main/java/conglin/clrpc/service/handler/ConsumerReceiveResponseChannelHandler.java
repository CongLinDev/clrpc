package conglin.clrpc.service.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.transport.component.InvocationExecutor;
import conglin.clrpc.transport.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ConsumerReceiveResponseChannelHandler extends SimpleChannelInboundHandler<Message> implements ComponentContextAware, Initializable {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerReceiveResponseChannelHandler.class);

    private ComponentContext context;
    private InvocationExecutor invocationExecutor;

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public void init() {
        invocationExecutor = getContext().getWith(ComponentContextEnum.INVOCATION_EXECUTOR);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        invocationExecutor.receive(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("ExceptionCaught : ", cause);
        ctx.close();
    }
}
