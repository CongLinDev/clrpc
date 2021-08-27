package conglin.clrpc.service.handler.factory;

import java.util.Collection;

import conglin.clrpc.common.Initializable;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import io.netty.channel.ChannelHandler;

/**
 * {@link ChannelHandlerFactory} 的实现类应当提供一个无参构造方法
 */
public interface ChannelHandlerFactory {

    /**
     * 反射创建 {ChannelHandlerFactory}
     * 
     * @param qualifiedClassName
     * @param context
     * @return
     */
    static ChannelHandlerFactory newFactory(String qualifiedClassName, RpcContext context) {
        ChannelHandlerFactory factory = null;
        if (qualifiedClassName != null) {
            factory = ClassUtils.loadObjectByType(qualifiedClassName, ChannelHandlerFactory.class);
        }
        if (factory == null) {
            // fallback default factory
           factory = new DefaultChannelHandlerFactory();
        }
        if (factory instanceof ContextAware) {
            ((ContextAware)factory).setContext(context);
        }
        if (factory instanceof Initializable) {
            ((Initializable)factory).init();
        }
        return factory;
    }

    /**
     * 处理器
     *
     * @return
     */
    Collection<ChannelHandler> handlers();
}