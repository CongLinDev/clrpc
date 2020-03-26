package conglin.clrpc.transport.handler;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.CommonContext;
import conglin.clrpc.transport.handler.codec.CommonDecoder;
import conglin.clrpc.transport.handler.codec.CommonEncoder;
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
        addCodecHandler();
        doInitChannel(ch);
        LOGGER.info("Here are ChannelHandlers in Channel(id={}) as follows.", ch.id().asLongText());
        pipeline.forEach(entry -> LOGGER.info("Name={}\tType={}", entry.getKey(), getChannelHandlerType(entry.getValue())));
    }

    /**
     * 获取 {@link ChannelHandler} 类型
     * 
     * @param handler
     * @return
     */
    private String getChannelHandlerType(ChannelHandler handler) {
        boolean isInbound = handler instanceof ChannelInboundHandler;
        boolean isOutbound = handler instanceof ChannelOutboundHandler;

        if (isInbound && isOutbound) {
            return "Inbound & Outbound";
        } else if (isInbound) {
            return "Inbound";
        } else if (isOutbound) {
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

    /**
     * 向 {@link io.netty.channel.ChannelPipeline} 中添加默认的编解码处理器
     */
    protected void addCodecHandler() {
        SerializationHandler serializationHandler = context().getSerializationHandler();
        pipeline().addLast("CommonEncoder", new CommonEncoder(serializationHandler)).addLast("CommonDecoder",
                new CommonDecoder(serializationHandler));
    }

    /**
     * 返回关联的上下文
     * 
     * @return
     */
    abstract protected CommonContext context();
}