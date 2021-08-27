package conglin.clrpc.service.handler.factory;

import conglin.clrpc.global.role.Role;
import conglin.clrpc.service.context.ContextAware;
import conglin.clrpc.service.context.RpcContext;
import conglin.clrpc.service.context.RpcContextEnum;
import conglin.clrpc.service.handler.ConsumerBasicServiceChannelHandler;
import conglin.clrpc.service.handler.ProviderBasicServiceChannelHandler;
import conglin.clrpc.transport.handler.ConsumerRequestChannelHandler;
import conglin.clrpc.transport.handler.ProviderResponseChannelHandler;
import conglin.clrpc.transport.handler.codec.RpcProtocolCodec;
import io.netty.channel.ChannelHandler;

import java.util.*;

/**
 * 对于Consumer端的 {@link io.netty.channel.ChannelPipeline}
 * 
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
 *  |    |                RpcProtocolCodecHandler                  |    |
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
 */

 
/**
 * 对于Provider端的 {@link io.netty.channel.ChannelPipeline}
 * 
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
 *  |    |                RpcProtocolCodecHandler                  |    |
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
 */

public class DefaultChannelHandlerFactory implements ChannelHandlerFactory, ContextAware {

    private RpcContext context;

    @Override
    public void setContext(RpcContext context) {
        this.context = context;
    }

    @Override
    public RpcContext getContext() {
        return context;
    }

    /**
     * 向编解码逻辑前加入 {@link ChannelHandler}
     *
     * @return
     */
    protected Collection<ChannelHandler> beforeCodec() {
        return Collections.emptyList();
    }

    /**
     * 向处理逻辑前加入 {@link ChannelHandler}
     *
     * @return
     */
    protected Collection<ChannelHandler> beforeHandle() {
        return Collections.emptyList();
    }

    /**
     * 向处理逻辑后加入 {@link ChannelHandler}
     *
     * @return
     */
    protected Collection<ChannelHandler> afterHandle() {
        return Collections.emptyList();
    }

    /**
     * 消息处理 {@link ChannelHandler}
     *
     * @return
     */
    protected Collection<ChannelHandler> messageHandle() {
        Role role = getContext().getWith(RpcContextEnum.ROLE);
        if (role.isConsumer()) {
            ConsumerBasicServiceChannelHandler consumerBasicServiceChannelHandler = new ConsumerBasicServiceChannelHandler();
            consumerBasicServiceChannelHandler.setContext(getContext());
            consumerBasicServiceChannelHandler.init();
            return Arrays.asList(consumerBasicServiceChannelHandler, new ConsumerRequestChannelHandler());
        } else if (role.isProvider()) {
            ProviderBasicServiceChannelHandler providerBasicServiceChannelHandler = new ProviderBasicServiceChannelHandler();
            providerBasicServiceChannelHandler.setContext(getContext());
            providerBasicServiceChannelHandler.init();
            return Arrays.asList(providerBasicServiceChannelHandler, new ProviderResponseChannelHandler());
        }
        return Collections.emptyList();
    }

    /**
     * 编解码 {@link ChannelHandler}
     *
     * @return
     */
    protected Collection<ChannelHandler> codecHandler() {
        return Collections.singletonList(new RpcProtocolCodec(getContext().getWith(RpcContextEnum.SERIALIZATION_HANDLER)));
    }

    @Override
    public Collection<ChannelHandler> handlers() {
        List<ChannelHandler> channelHandlerList = new ArrayList<>();
        // before codec
        channelHandlerList.addAll(beforeCodec());
        // codec
        channelHandlerList.addAll(codecHandler());
        // after codec before handle
        channelHandlerList.addAll(beforeHandle());
        // handle message
        channelHandlerList.addAll(messageHandle());
        // after handle
        channelHandlerList.addAll(afterHandle());

        return channelHandlerList;
    }
}
