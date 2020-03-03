package conglin.clrpc.transport.handler;

import java.util.Collections;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.handler.ConsumerBasicServiceChannelHandler;
import conglin.clrpc.transport.handler.codec.CommonDecoder;
import conglin.clrpc.transport.handler.codec.CommonEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
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
 *  |    +----------+----------+            +-----------+----------+    |
 *  |    |       Decoder       |            |        Encoder       |    |
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
public class ConsumerChannelInitializer extends AbstractChannelInitializer {

    protected final ConsumerContext context;

    public ConsumerChannelInitializer(ConsumerContext context) {
        super();
        this.context = context;
    }

    @Override
    protected void doInitChannel(SocketChannel ch) throws Exception {
        SerializationHandler serializationHandler = context.getSerializationHandler();
        pipeline().addLast("Common Encoder", new CommonEncoder(serializationHandler)).addLast("Common Decoder",
                new CommonDecoder(serializationHandler));
        // before handle request
        addChannelHandlers(context.getPropertyConfigurer().getOrDefault("consumer.channel-handler.before",
                Collections.emptyList()));
        // ansyc handle request
        pipeline().addLast("ConsumerBasicServiceChannelHandler", new ConsumerBasicServiceChannelHandler(context))
                // ansyc handle response
                .addLast("ConsumerRequestChannelHandler", new ConsumerRequestChannelHandler());
        // after handle request
        addChannelHandlers(context.getPropertyConfigurer().getOrDefault("consumer.channel-handler.after",
                Collections.emptyList()));
    }

    @Override
    protected ChannelHandler getChannelHandlerObject(String qualifiedClassName) {
        return ClassUtils.loadClassObject(qualifiedClassName, ChannelHandler.class, context);
    }
}