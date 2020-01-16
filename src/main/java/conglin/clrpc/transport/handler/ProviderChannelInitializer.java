package conglin.clrpc.transport.handler;

import java.util.ArrayList;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.handler.codec.BasicResponseEncoder;
import conglin.clrpc.transport.handler.codec.CommonDecoder;
import io.netty.channel.ChannelHandler;
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
 *  |  | TransactionChannelHandler |                    |               |
 *  |  +------------+--------------+                    |               |
 *  |              /|\                                  |               |
 *  |               |                                   |               |
 *  |  +------------+--------------+                    |               |
 *  |  | BasicServiceChannelHandler|                    |               |
 *  |  +------------+--------------+                   \|/              |
 *  |              /|\                                  .               |
 *  |               .                                   .               |
 *  |      Before handling request, you can add some ChannelHandlers.   |
 *  |               .                                   .               |
 *  |               .                                  \|/              |
 *  |    +----------+----------+            +-----------+----------+    |
 *  |    |       Decoders      |            |        Encoders      |    |
 *  |    +----------+----------+            +-----------+----------+    |
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
    protected void doInitChannel(SocketChannel ch) throws Exception {
        SerializationHandler serializationHandler = context.getSerializationHandler();
        pipeline().addLast("Common Decoder", new CommonDecoder(serializationHandler)).addLast("BasicResponse Encoder",
                new BasicResponseEncoder(serializationHandler));
        // before handle request
        addChannelHandlers(context.getPropertyConfigurer().getOrDefault("provider.channel-handler.before-handle", new ArrayList<String>()));
        // handle request
        pipeline().addLast("ProviderBasicServiceChannelHandler", new ProviderBasicServiceChannelHandler(context)).addLast(
                "ProviderTransactionServiceChannelHandler", new ProviderTransactionServiceChannelHandler(context));
        // after handle request
        addChannelHandlers(context.getPropertyConfigurer().getOrDefault("provider.channel-handler.after-handle", new ArrayList<String>()));
        // send response
        pipeline().addLast("ProviderResponseChannelHandler", new ProviderResponseChannelHandler());
    }

    @Override
    protected ChannelHandler getChannelHandlerObject(String qualifiedClassName) {
        return ClassUtils.loadClassObject(ChannelHandler.class, qualifiedClassName, context);
    }

}