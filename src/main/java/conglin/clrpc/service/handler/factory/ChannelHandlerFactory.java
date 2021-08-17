package conglin.clrpc.service.handler.factory;

import java.util.Collection;
import java.util.Collections;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.channel.CommonChannelContext;
import io.netty.channel.ChannelHandler;

/**
 * {@link ChannelHandlerFactory} 的实现类应当提供一个有参构造方法
 * 
 * 其中参数应当为 {@link conglin.clrpc.service.context.channel.CommonChannelContext}
 */
public interface ChannelHandlerFactory {

    /**
     * 反射创建 {ChannelHandlerFactory}
     * 
     * @param qualifiedClassName
     * @param context
     * @return
     */
    static ChannelHandlerFactory newFactory(String qualifiedClassName, CommonChannelContext context) {
        if (qualifiedClassName == null) {
            return new ChannelHandlerFactory() { };
        }
        ChannelHandlerFactory factory = ClassUtils.loadObjectByType(qualifiedClassName, ChannelHandlerFactory.class, context);
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