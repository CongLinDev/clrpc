package conglin.clrpc.service.handler.factory;

import io.netty.channel.ChannelHandler;

import java.util.Collection;

/**
 * {@link ChannelHandlerFactory} 的实现类应当提供一个无参构造方法
 */
public interface ChannelHandlerFactory {
    /**
     * 处理器
     *
     * @return 处理器集合
     */
    Collection<ChannelHandler> handlers();
}