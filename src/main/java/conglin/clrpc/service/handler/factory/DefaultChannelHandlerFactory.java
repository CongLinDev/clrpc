package conglin.clrpc.service.handler.factory;

import conglin.clrpc.definition.role.Role;
import conglin.clrpc.service.context.ComponentContextAware;
import conglin.clrpc.service.context.ComponentContext;
import conglin.clrpc.service.context.ComponentContextEnum;
import conglin.clrpc.service.handler.ConsumerBasicServiceChannelHandler;
import conglin.clrpc.service.handler.ProviderBasicServiceChannelHandler;
import conglin.clrpc.transport.handler.ConsumerRequestChannelHandler;
import conglin.clrpc.transport.handler.ProviderResponseChannelHandler;
import conglin.clrpc.transport.handler.codec.UniProtocolCodec;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 对于Consumer端的 {@link io.netty.channel.ChannelPipeline}
 * <p>
 * 其目前主要由下面的 {@link io.netty.channel.ChannelHandler} 组成
 *
 * <pre>
 *                                                 I/O Request
 *                                            via {@link Channel} or
 *                                        {@link ChannelHandlerContext}
 *                                                      |
 *  +---------------------------------------------------+---------------+
 *  |                           ChannelPipeline         |               |
 *  |                                                  \|/              |
 *  |              /|\                                  .               |
 *  |               .                                   .               |
 *  |      After handling request, you can add some ChannelHandlers.    |
 *  |               .                                   .               |
 *  |               .                                  \|/              |
 *  |               |                                   |               |
 *  |  +------------+--------------+                    |               |
 *  |  |   RequestChannelHandler   |                    |               |
 *  |  +------------+--------------+                   \|/              |
 *  |              /|\                                  .               |
 *  |  +------------+--------------+                    |               |
 *  |  | BasicServiceChannelHandler|                    |               |
 *  |  +------------+--------------+                    |               |
 *  |               .                                   .               |
 *  |      Before handling request, you can add some ChannelHandlers.   |
 *  |               .                                   .               |
 *  |               .                                   .               |
 *  |              /|\                                 \|/              |
 *  |    +----------+----------+------------+-----------+----------+    |
 *  |    |                     UniProtocolCodec                    |    |
 *  |    +----------+----------+------------+-----------+----------+    |
 *  |              /|\                                  |               |
 *  +---------------+-----------------------------------+---------------+
 *                  |                                  \|/
 *  +---------------+-----------------------------------+---------------+
 *  |               |                                   |               |
 *  |       [ Socket.read() ]                    [ Socket.write() ]     |
 *  |                                                                   |
 *  |  Netty Internal I/O Threads (Transport Implementation)            |
 *  +-------------------------------------------------------------------+
 *
 * </pre>
 * </p>

 * 对于Provider端的 {@link io.netty.channel.ChannelPipeline}
 * <p>
 * 其目前主要由下面的 {@link io.netty.channel.ChannelHandler} 组成
 *
 *
 * <pre>
 *                                                 I/O Request
 *                                            via {@link Channel} or
 *                                        {@link ChannelHandlerContext}
 *                                                      |
 *  +---------------------------------------------------+---------------+
 *  |                           ChannelPipeline         |               |
 *  |                                                  \|/              |
 *  |  +------------+--------------+                    |               |
 *  |  |   ResponseChannelHandler  |                    |               |
 *  |  +------------+--------------+                    |               |
 *  |              /|\                                  .               |
 *  |               .                                   .               |
 *  |      After handling request, you can add some ChannelHandlers.    |
 *  |               .                                   .               |
 *  |               .                                  \|/              |
 *  |               |                                   |               |
 *  |  +------------+--------------+                    |               |
 *  |  | BasicServiceChannelHandler|                    |               |
 *  |  +------------+--------------+                    |               |
 *  |              /|\                                  |               |
 *  |               |                                   |               |
 *  |  +------------+--------------+                    |               |
 *  |  | TransactionChannelHandler |                    |               |
 *  |  +------------+--------------+                   \|/              |
 *  |              /|\                                  .               |
 *  |               .                                   .               |
 *  |      Before handling request, you can add some ChannelHandlers.   |
 *  |               .                                   .               |
 *  |               .                                  \|/              |
 *  |    +----------+----------+------------+-----------+----------+    |
 *  |    |                     UniProtocolCodec                    |    |
 *  |    +----------+----------+------------+-----------+----------+    |
 *  |              /|\                                  |               |
 *  +---------------+-----------------------------------+---------------+
 *                  |                                  \|/
 *  +---------------+-----------------------------------+---------------+
 *  |               |                                   |               |
 *  |       [ Socket.read() ]                    [ Socket.write() ]     |
 *  |                                                                   |
 *  |  Netty Internal I/O Threads (Transport Implementation)            |
 *  +-------------------------------------------------------------------+
 *
 * </pre>
 * </p>
 */

public class DefaultChannelHandlerFactory implements OrderedChannelHandlerFactory, ComponentContextAware {

    private ComponentContext context;

    @Override
    public void setContext(ComponentContext context) {
        this.context = context;
    }

    @Override
    public ComponentContext getContext() {
        return context;
    }

    @Override
    public Collection<OrderedChannelHandler> disorderlyHandlers() {
        List<OrderedChannelHandler> channelHandlerList = new ArrayList<>();
        channelHandlerList.add(new DefaultOrderedChannelHandler(new UniProtocolCodec(), ChannelHandlerPhase.CODEC));

        Role role = getContext().getWith(ComponentContextEnum.ROLE);
        if (role.isConsumer()) {
            channelHandlerList.add(new DefaultOrderedChannelHandler(new ConsumerBasicServiceChannelHandler(), ChannelHandlerPhase.HANDLE, 1));
            channelHandlerList.add(new DefaultOrderedChannelHandler(new ConsumerRequestChannelHandler(), ChannelHandlerPhase.HANDLE, 2));
        } else if (role.isProvider()) {
            channelHandlerList.add(new DefaultOrderedChannelHandler(new ProviderBasicServiceChannelHandler(), ChannelHandlerPhase.HANDLE, 1));
            channelHandlerList.add(new DefaultOrderedChannelHandler(new ProviderResponseChannelHandler(), ChannelHandlerPhase.HANDLE, 2));
        }
        return channelHandlerList;
    }
}
