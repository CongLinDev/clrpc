package conglin.clrpc.service.handler.factory;

import java.util.Collection;
import java.util.Collections;

import conglin.clrpc.common.util.ClassUtils;
import io.netty.channel.ChannelHandler;

/**
 * {@link ChannelHandlerFactory} 的实现类应当提供一个有参构造方法
 * 
 * 其中参数应当为 {@link }
 */
public interface ChannelHandlerFactory {

    /**
     * 反射创建 {ChannelHandlerFactory}
     * 
     * @param qualifiedClassName
     * @param args
     * @return
     */
    static ChannelHandlerFactory newFactory(String qualifiedClassName, Object... args) {
        ChannelHandlerFactory factory = ClassUtils.loadClassObject(qualifiedClassName, ChannelHandlerFactory.class,
                args);
        if (factory == null)
            factory = new ChannelHandlerFactory() {};
        return factory;
    }

    /**
     * 向 clrpc 处理逻辑前加入 {@link ChannelHandler}
     * 
     * @return
     */
    default Collection<ChannelHandler> before() {
        return Collections.emptyList();
    }

    /**
     * 向 clrpc 处理逻辑后加入 {@link ChannelHandler}
     * 
     * @return
     */
    default Collection<ChannelHandler> after() {
        return Collections.emptyList();
    }
}