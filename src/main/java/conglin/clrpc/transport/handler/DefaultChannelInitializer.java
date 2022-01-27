package conglin.clrpc.transport.handler;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.handler.factory.ChannelHandlerFactory;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> implements ContextAware, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChannelInitializer.class);

    protected ChannelHandlerFactory channelHandlerFactory;
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
        Properties properties = getContext().getWith(RpcContextEnum.PROPERTIES);
        Role role = getContext().getWith(RpcContextEnum.ROLE);
        String factoryClass = properties.getProperty(role.item(".channel.handler-factory"));
        channelHandlerFactory = ChannelHandlerFactory.newFactory(factoryClass);
        ObjectLifecycleUtils.assemble(channelHandlerFactory, getContext());
    }

    @Override
    final protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        for (ChannelHandler handler : channelHandlerFactory.handlers()) {
            ObjectLifecycleUtils.assemble(handler, getContext());
            pipeline.addLast(handler);
        }
        LOGGER.info("Here are ChannelHandlers in Channel(id={}) as follows.", ch.id().asShortText());
        pipeline.forEach(entry -> LOGGER.info("Type={}\tName={}", getChannelHandlerType(entry.getValue()), entry.getKey()));
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