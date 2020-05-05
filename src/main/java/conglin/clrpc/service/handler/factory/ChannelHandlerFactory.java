package conglin.clrpc.service.handler.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

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
     * @param args
     * @return
     */
    static ChannelHandlerFactory newFactory(String qualifiedClassName, CommonChannelContext context) {
        if (qualifiedClassName == null)
            return new ChannelHandlerFactory() {
            };

        try {
            Class<? extends ChannelHandlerFactory> clazz = Class.forName(qualifiedClassName)
                    .asSubclass(ChannelHandlerFactory.class);

            Constructor<?> constructor = null, baseConstructor = null;

            for (Constructor<?> c : clazz.getConstructors()) {
                Class<?>[] types = c.getParameterTypes();
                if (types.length == 1) {
                    if (types[0] == context.getClass()) {
                        constructor = c;
                    } else if (types[0] == CommonChannelContext.class) {
                        baseConstructor = c;
                    }
                }
            }

            return (ChannelHandlerFactory) (constructor != null ? constructor.newInstance(context)
                    : baseConstructor.newInstance(context));

        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException | SecurityException | NullPointerException e) {
            return new ChannelHandlerFactory() {
            };
        }
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