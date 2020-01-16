package conglin.clrpc.transport.handler;

import java.util.ArrayList;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.transport.handler.codec.BasicRequestEncoder;
import conglin.clrpc.transport.handler.codec.CommonDecoder;
import conglin.clrpc.transport.handler.codec.TransactionRequestEncoder;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
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
 *  |               .                                   .               |
 *  |      Before handling request, you can add some ChannelHandlers.   |
 *  |               .                                   .               |
 *  |               .                                   .               |
 *  |  +------------+--------------+                    |               |
 *  |  | BasicServiceChannelHandler|                    |               |
 *  |  +------------+--------------+                    |               |
 *  |              /|\                                 \|/              |
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
public class ConsumerChannelInitializer extends AbstractChannelInitializer {

    protected final ConsumerContext context;

    public ConsumerChannelInitializer(ConsumerContext context) {
        super();
        this.context = context;
    }

    @Override
    protected void doInitChannel(SocketChannel ch) throws Exception {
        SerializationHandler serializationHandler = context.getSerializationHandler();
        pipeline().addLast("BasicRequest Encoder", new BasicRequestEncoder(serializationHandler))
                .addLast("TransactionRequest Encoder", new TransactionRequestEncoder(serializationHandler))
                .addLast("Common Decoder", new CommonDecoder(serializationHandler))
                // handle response
                .addLast("ConsumerBasicServiceChannelHandler", new ConsumerBasicServiceChannelHandler(context));
        // before handle request
        addChannelHandlers(context.getPropertyConfigurer().getOrDefault("consumer.channel-handler.before-handle", new ArrayList<String>()));
        // handle request
        pipeline().addLast("ConsumerRequestChannelHandler", new ConsumerRequestChannelHandler());
        // after handle request
        addChannelHandlers(context.getPropertyConfigurer().getOrDefault("consumer.channel-handler.after-handle", new ArrayList<String>()));
    }

    @Override
    protected ChannelHandler getChannelHandlerObject(String qualifiedClassName) {
        return ClassUtils.loadClassObject(ChannelHandler.class, qualifiedClassName, context);
    }
}