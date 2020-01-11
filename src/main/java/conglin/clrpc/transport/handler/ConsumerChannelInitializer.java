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
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelPipeline pipeline;

    protected final ConsumerContext context;

    public ConsumerChannelInitializer(ConsumerContext context) {
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        this.pipeline = ch.pipeline();
        SerializationHandler serializationHandler = context.getSerializationHandler();
        pipeline.addLast("BasicRequest Encoder", new BasicRequestEncoder(serializationHandler))
                .addLast("TransactionRequest Encoder", new TransactionRequestEncoder(serializationHandler))
                .addLast("Common Decoder", new CommonDecoder(serializationHandler))
                // handle response
                .addLast("ConsumerBasicServiceChannelHandler", new ConsumerBasicServiceChannelHandler(context));
        // before handle request
        initChannelBeforeHandleRequest(pipeline);
        // handle request
        pipeline.addLast("ConsumerRequestChannelHandler", new ConsumerRequestChannelHandler());
        // after handle request
        initChannelAfterHandleRequest(pipeline);
    }

    /**
     * 返回当前绑定的 {@link io.netty.channel.Channel}
     * 
     * @return
     */
    public Channel channel() {
        return pipeline.channel();
    }

    /**
     * 初始化通道 向管道上添加 请求处理器 之前的处理器
     * 
     * 支持Inbound和Outbound处理器,但是考虑到实际处理顺序,最好添加Inbound处理器
     * 
     * @param pipeline
     */
    protected void initChannelBeforeHandleRequest(ChannelPipeline pipeline) {
        context.getPropertyConfigurer().getOrDefault("consumer.channel-handler.before-handle", new ArrayList<String>())
                .stream().map(this::getChannelHandlerObject).forEach(pipeline::addLast);
    }

    /**
     * 初始化通道 向管道上添加 请求处理器 之后的处理器
     * 
     * 支持Inbound和Outbound处理器,但是考虑到实际处理顺序,最好添加Inbound处理器
     * 
     * @param pipeline
     */
    protected void initChannelAfterHandleRequest(ChannelPipeline pipeline) {
        context.getPropertyConfigurer().getOrDefault("consumer.channel-handler.after-handle", new ArrayList<String>())
                .stream().map(this::getChannelHandlerObject).forEach(pipeline::addLast);
    }

    /**
     * 构造 {@link io.netty.channel.ChannelHandler}
     * 
     * 构造方法应当是一个参数,且参数类型为 {@link conglin.clrpc.service.context.ProviderContext}
     * 
     * @param qualifiedClassName
     * @return
     */
    protected ChannelHandler getChannelHandlerObject(String qualifiedClassName) {
        return ClassUtils.loadClassObject(ChannelHandler.class, qualifiedClassName, context);
    }
}