package conglin.clrpc.netty;

import conglin.clrpc.executor.ResponseExecutor;
import conglin.clrpc.executor.ReverseExecutor;
import conglin.clrpc.executor.pipeline.ExecutorPipeline;
import conglin.clrpc.invocation.protocol.ProtocolDefinition;
import conglin.clrpc.invocation.serialization.SerializationHandler;
import conglin.clrpc.netty.handler.ReceiveMessageChannelHandler;
import conglin.clrpc.netty.handler.UniProtocolCodec;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

public class ProviderInitializer extends ChannelInitializer<SocketChannel> {

    private final ExecutorPipeline executorPipeline;
    private final SerializationHandler serializationHandler;
    private final ProtocolDefinition protocolDefinition;

    public ProviderInitializer(ExecutorPipeline executorPipeline, SerializationHandler serializationHandler, ProtocolDefinition protocolDefinition) {
        this.executorPipeline = executorPipeline;
        this.serializationHandler = serializationHandler;
        this.protocolDefinition = protocolDefinition;
    }

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        executorPipeline.register(new ReverseExecutor());
        executorPipeline.register(new ResponseExecutor(pipeline::writeAndFlush));
        pipeline.addLast(new UniProtocolCodec(serializationHandler, protocolDefinition));
        pipeline.addLast(new ReceiveMessageChannelHandler(executorPipeline));    
    }

}