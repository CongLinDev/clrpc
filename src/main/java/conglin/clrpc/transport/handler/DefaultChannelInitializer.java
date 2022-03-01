package conglin.clrpc.transport.handler;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.definition.role.Role;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.handler.factory.ChannelHandlerFactory;
import conglin.clrpc.service.handler.factory.DefaultChannelHandlerFactory;
import conglin.clrpc.service.util.ObjectLifecycleUtils;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class DefaultChannelInitializer extends ChannelInitializer<SocketChannel> implements ComponentContextAware, Initializable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultChannelInitializer.class);

    protected ChannelHandlerFactory channelHandlerFactory;
    private ComponentContext context;

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
        Properties properties = getContext().getWith(ComponentContextEnum.PROPERTIES);
        Role role = getContext().getWith(ComponentContextEnum.ROLE);
        String factoryClassName = properties.getProperty(role.item(".channel.handler-factory"));
        channelHandlerFactory = newChannelHandlerFactory(factoryClassName);
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

    /**
     * 反射创建 {@link ChannelHandlerFactory}
     * 
     * @param qualifiedClassName 全限定名
     * @param context 上下文
     * @return 工厂对象
     */
    protected ChannelHandlerFactory newChannelHandlerFactory(String qualifiedClassName) {
        ChannelHandlerFactory factory = null;
        if (qualifiedClassName != null) {
            factory = ClassUtils.loadObjectByType(qualifiedClassName, ChannelHandlerFactory.class);
        }
        if (factory == null) {
            // fallback default factory
           factory = new DefaultChannelHandlerFactory();
        }
        ObjectLifecycleUtils.assemble(factory, getContext());
        return factory;
    }
}