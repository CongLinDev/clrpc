package conglin.clrpc.service.handler.factory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Collections;

import conglin.clrpc.service.context.CommonContext;
import io.netty.channel.ChannelHandler;

/**
 * {@link ChannelHandlerFactory} 的实现类应当提供一个有参构造方法
 * 
 * 其中参数应当为 {@link conglin.clrpc.service.context.CommonContext}
 */
public interface ChannelHandlerFactory {

    static ChannelHandler[] EMPTY_HANDLER = new ChannelHandler[0];

    /**
     * 反射创建 {ChannelHandlerFactory}
     * 
     * @param qualifiedClassName
     * @param args
     * @return
     */
    static ChannelHandlerFactory newFactory(String qualifiedClassName, CommonContext context) {
        if (qualifiedClassName == null)
            return new ChannelHandlerFactory(){};

        try {
            Class<? extends ChannelHandlerFactory> clazz = Class.forName(qualifiedClassName)
                .asSubclass(ChannelHandlerFactory.class);
            Constructor<? extends ChannelHandlerFactory> constructor = clazz.getConstructor(CommonContext.class);
            constructor.setAccessible(true);
            return constructor.newInstance(context);
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | IllegalArgumentException
            | InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return new ChannelHandlerFactory(){};
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