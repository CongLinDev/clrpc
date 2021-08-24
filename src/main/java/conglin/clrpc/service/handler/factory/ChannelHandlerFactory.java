package conglin.clrpc.service.handler.factory;

import java.util.Collection;
import java.util.Collections;

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
        if (qualifiedClassName == null) {
            return new ChannelHandlerFactory() { };
        }
        ChannelHandlerFactory factory = ClassUtils.loadObjectByType(qualifiedClassName, ChannelHandlerFactory.class);
        if (factory instanceof ContextAware) {
            ((ContextAware)factory).setContext(context);
        }
        return factory != null ? factory : new ChannelHandlerFactory() { };
    }

    /**
     * 向 clrpc 编解码逻辑前加入 {@link ChannelHandler}
     * 
     * @return
     */
    default Collection<ChannelHandler> beforeCodec() {
        return Collections.emptyList();
    }

    /**
     * 向 clrpc 处理逻辑前加入 {@link ChannelHandler}
     * 
     * @return
     */
    default Collection<ChannelHandler> beforeHandle() {
        return Collections.emptyList();
    }

    /**
     * 向 clrpc 处理逻辑后加入 {@link ChannelHandler}
     * 
     * @return
     */
    default Collection<ChannelHandler> afterHandle() {
        return Collections.emptyList();
    }
}