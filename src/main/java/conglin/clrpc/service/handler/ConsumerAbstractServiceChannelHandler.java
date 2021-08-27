package conglin.clrpc.service.handler;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.util.concurrent.ExecutorService;

abstract public class ConsumerAbstractServiceChannelHandler<T> extends SimpleChannelInboundHandler<T> implements ContextAware, Initializable {

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
    protected void channelRead0(ChannelHandlerContext ctx, T msg) throws Exception {
        ExecutorService executorService = context.getWith(RpcContextEnum.EXECUTOR_SERVICE);
        executorService.submit(() -> {
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
        LOGGER.error("ExceptionCaught : ", cause);
        ctx.close();
    }
}