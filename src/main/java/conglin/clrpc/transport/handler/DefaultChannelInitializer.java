package conglin.clrpc.transport.handler;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.handler.factory.ChannelHandlerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> implements ContextAware, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChannelInitializer.class);

    private RpcContext rpcContext;

    @Override
    public RpcContext getContext() {
        return rpcContext;
    }

    @Override
    public void setContext(RpcContext context) {
        rpcContext = context;
    }

    @Override
    public void init() {

    }

    @Override
    final protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();

        PropertyConfigurer c = getContext().getWith(RpcContextEnum.PROPERTY_CONFIGURER);
        Role role = getContext().getWith(RpcContextEnum.ROLE);

        String factoryClass = c.get(role.item(".channel.handler-factory"), String.class);
        ChannelHandlerFactory factory = ChannelHandlerFactory.newFactory(factoryClass, getContext());
        factory.handlers().forEach(pipeline::addLast);

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
            return "Unknown";
        }
    }
}