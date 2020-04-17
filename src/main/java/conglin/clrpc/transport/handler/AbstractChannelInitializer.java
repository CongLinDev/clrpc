package conglin.clrpc.transport.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.service.context.CommonContext;
import conglin.clrpc.service.handler.traffic.ChannelTrafficHandler;
import conglin.clrpc.transport.handler.codec.RpcProtocolCodec;
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

        if(context().getSerivceLogger().isEnable()) {
            pipeline().addLast(new ChannelTrafficHandler(context()));
        }

        addCodecHandler();
        doInitChannel(ch);
        LOGGER.info("Here are ChannelHandlers in Channel(id={}) as follows.", ch.id().asShortText());
        pipeline.forEach(
                entry -> LOGGER.info("Type={}\tName={}", getChannelHandlerType(entry.getValue()), entry.getKey()));
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
            return "Duplex";
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
     * 向 {@link io.netty.channel.ChannelPipeline} 中添加默认的编解码处理器
     */
    protected void addCodecHandler() {
        SerializationHandler serializationHandler = context().getSerializationHandler();
        pipeline().addLast("RpcProtocolCodec", new RpcProtocolCodec(serializationHandler));
    }

    /**
     * 返回关联的上下文
     * 
     * @return
     */
    abstract protected CommonContext context();
}