package conglin.clrpc.transport.handler;

import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.service.context.channel.CommonChannelContext;
import conglin.clrpc.service.context.channel.ProviderChannelContext;
import conglin.clrpc.service.handler.ProviderBasicServiceChannelHandler;
import conglin.clrpc.service.handler.ProviderTransactionServiceChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.socket.SocketChannel;

/**
 * 该类负责构建RPC的Provider端的 {@link io.netty.channel.ChannelPipeline}
 * 
 * 其目前主要下面的 {@link io.netty.channel.ChannelHandler} 组成
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
public class ProviderChannelInitializer extends AbstractChannelInitializer {

    private final ProviderContext context;

    public ProviderChannelInitializer(ProviderContext context) {
        super();
        this.context = context;
    }

    @Override
    protected ProviderChannelContext channelContext() {
        return new ProviderChannelContext(context);
    }

    @Override
    protected void doInitChannel(SocketChannel ch, CommonChannelContext channelContext) throws Exception {
        ProviderChannelContext context = (ProviderChannelContext)channelContext;
        // handle request
        pipeline()
                .addLast("ProviderTransactionServiceChannelHandler",
                        new ProviderTransactionServiceChannelHandler(context))
                .addLast("ProviderBasicServiceChannelHandler", new ProviderBasicServiceChannelHandler(context));

        // send response
        pipeline().addLast("ProviderResponseChannelHandler", new ProviderResponseChannelHandler());
    }
}