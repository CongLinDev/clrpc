package conglin.clrpc.netty.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.executor.pipeline.ExecutorPipeline;
import conglin.clrpc.invocation.message.Message;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ReceiveMessageChannelHandler extends SimpleChannelInboundHandler<Message> {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveMessageChannelHandler.class);

    private final ExecutorPipeline executorPipeline;

    public ReceiveMessageChannelHandler(ExecutorPipeline executorPipeline) {
        this.executorPipeline = executorPipeline;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        executorPipeline.inbound(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("ExceptionCaught : ", cause);
        ctx.close();
    }
}
