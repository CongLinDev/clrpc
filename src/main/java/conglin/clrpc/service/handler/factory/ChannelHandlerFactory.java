package conglin.clrpc.service.handler.factory;

import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.util.ObjectAssemblyUtils;
import io.netty.channel.ChannelHandler;

import java.util.Collection;

/**
 * {@link ChannelHandlerFactory} 的实现类应当提供一个无参构造方法
 */
public interface ChannelHandlerFactory {

    /**
     * 反射创建 {@link ChannelHandlerFactory}
     * 
     * @param qualifiedClassName 全限定名
     * @param context 上下文
     * @return 工厂对象
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
        ObjectAssemblyUtils.assemble(factory, context);
        return factory;
    }

    /**
     * 处理器
     *
     * @return 处理器集合
     */
    Collection<ChannelHandler> handlers();
}