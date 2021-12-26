package conglin.clrpc.service.handler;

import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.transport.message.Message;
import conglin.clrpc.transport.message.Payload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ExecutorService;

abstract public class ConsumerAbstractServiceChannelHandler extends SimpleChannelInboundHandler<Message> implements ContextAware {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerAbstractServiceChannelHandler.class);

    private RpcContext context;

    @Override
    public RpcContext getContext() {
        return context;
    }

    @Override
    public void setContext(RpcContext context) {
        this.context = context;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Message msg) throws Exception {
        if (accept(msg.payload())) {
            ExecutorService executorService = context.getWith(RpcContextEnum.EXECUTOR_SERVICE);
            executorService.submit(() -> {
                Object result = execute(msg.messageId(), msg.payload());
                if(result != null)
                    ctx.fireChannelRead(result);
            });
        } else {
            ctx.fireChannelRead(msg);
        }
    }

    /**
     * 是否处理
     *
     * @param payload payload
     * @return result
     */
    abstract protected boolean accept(Payload payload);

    /**
     * 执行具体方法
     * 
     * @param payload payload
     * @return 执行后的结果
     */
    abstract protected Object execute(Long messageId, Payload payload);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        LOGGER.error("ExceptionCaught : ", cause);
        ctx.close();
    }
}