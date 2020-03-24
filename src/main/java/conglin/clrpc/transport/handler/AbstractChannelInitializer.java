package conglin.clrpc.transport.handler;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.CommonContext;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

abstract public class AbstractChannelInitializer extends ChannelInitializer<SocketChannel> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractChannelInitializer.class);

    private ChannelPipeline pipeline;

    @Override
    final protected void initChannel(SocketChannel ch) throws Exception {
        this.pipeline = ch.pipeline();
        doInitChannel(ch);
        LOGGER.info("Here are ChannelHandlers in ChannelPipeline as follows.");
        pipeline.forEach(this::logChannelHandler);
    }

    /**
     * 记录 {@link ChannelHandler}
     * 
     * @param entry
     */
    private void logChannelHandler(Map.Entry<String, ChannelHandler> entry) {
        ChannelHandler handler = entry.getValue();
        LOGGER.info("Name={}\tChannelHandler={}\tType={}", entry.getKey(), handler.getClass().getName(),
                getChannelHandlerType(handler));
    }

    /**
     * 获取 {@link ChannelHandler} 类型
     * 
     * @param handler
     * @return
     */
    private String getChannelHandlerType(ChannelHandler handler) {
        if (handler instanceof ChannelInboundHandler) {
            return "Inbound";
        } else if (handler instanceof ChannelOutboundHandler) {
            return "Outbound";
        } else {
            return "Unkonwn";
        }
    }

    /**
     * 初始化Channel具体方法
     * 
     * @param ch
     * @throws Exception
     */
    abstract protected void doInitChannel(SocketChannel ch) throws Exception;

    /**
     * 返回当前绑定的 {@link io.netty.channel.ChannelPipeline}
     * 
     * @return
     */
    protected ChannelPipeline pipeline() {
        return this.pipeline;
    }

    /**
     * 在 {@link AbstractChannelInitializer#pipeline()} 后添加指定的
     * {@link io.netty.channel.ChannelHandler}
     * 
     * @param handlerClassnames
     */
    protected void addChannelHandlers(List<String> handlerClassnames) {
        handlerClassnames.stream().map(
                qualifiedClassName -> ClassUtils.loadClassObject(qualifiedClassName, ChannelHandler.class, context()))
                .forEach(pipeline::addLast);
    }

    abstract protected CommonContext context();

}