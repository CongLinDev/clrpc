package conglin.clrpc.transport.handler;

import java.util.ArrayList;

import conglin.clrpc.common.serialization.SerializationHandler;
import conglin.clrpc.common.util.ClassUtils;
import conglin.clrpc.service.context.ProviderContext;
import conglin.clrpc.transport.handler.codec.BasicResponseEncoder;
import conglin.clrpc.transport.handler.codec.CommonDecoder;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {

    private final ProviderContext context;

    public ProviderChannelInitializer(ProviderContext context) {
        super();
        this.context = context;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        SerializationHandler serializationHandler = context.getSerializationHandler();
        pipeline.addLast("Common Decoder", new CommonDecoder(serializationHandler)).addLast("BasicResponse Encoder",
                new BasicResponseEncoder(serializationHandler));
        // before handle request
        initChannelBeforeHandleRequest(pipeline);
        // handle request
        pipeline.addLast("ProviderBasicServiceChannelHandler", new ProviderBasicServiceChannelHandler(context)).addLast(
                "ProviderTransactionServiceChannelHandler", new ProviderTransactionServiceChannelHandler(context));
        // after handle request
        initChannelAfterHandleRequest(pipeline);
        // send response
        pipeline.addLast("ProviderResponseChannelHandler", new ProviderResponseChannelHandler());
    }

    /**
     * 初始化通道 向管道上添加 请求处理器 之前的处理器
     * 
     * 支持Inbound和Outbound处理器,但是考虑到实际处理顺序,最好添加Inbound处理器
     * 
     * @param pipeline
     */
    protected void initChannelBeforeHandleRequest(ChannelPipeline pipeline) {
        context.getPropertyConfigurer().getOrDefault("provider.channel-handler.before-handle", new ArrayList<String>())
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
        context.getPropertyConfigurer().getOrDefault("provider.channel-handler.after-handle", new ArrayList<String>())
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