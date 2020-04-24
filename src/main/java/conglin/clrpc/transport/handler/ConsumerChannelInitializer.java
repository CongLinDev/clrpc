package conglin.clrpc.transport.handler;

import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.context.channel.CommonChannelContext;
import conglin.clrpc.service.context.channel.ConsumerChannelContext;
import conglin.clrpc.service.handler.ConsumerBasicServiceChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

/**
 * 该类负责构建RPC的Consumer端的 {@link io.netty.channel.ChannelPipeline}
 * 
 * 其目前主要下面的 {@link io.netty.channel.ChannelHandler} 组成
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
public class ConsumerChannelInitializer extends AbstractChannelInitializer {

    protected final ConsumerContext context;

    public ConsumerChannelInitializer(ConsumerContext context) {
        super();
        this.context = context;
    }

    @Override
    protected ConsumerChannelContext channelContext() {
        return new ConsumerChannelContext(context);
    }

    @Override
    protected void doInitChannel(SocketChannel ch, CommonChannelContext channelContext) throws Exception {
        ConsumerChannelContext context = (ConsumerChannelContext) channelContext;
        // ansyc handle request
        pipeline().addLast("ConsumerBasicServiceChannelHandler", new ConsumerBasicServiceChannelHandler(context))
                // ansyc handle response
                .addLast("ConsumerRequestChannelHandler", new ConsumerRequestChannelHandler());
    }
}