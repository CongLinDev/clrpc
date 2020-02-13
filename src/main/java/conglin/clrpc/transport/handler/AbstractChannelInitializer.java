package conglin.clrpc.transport.handler;

import java.util.List;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;

abstract public class AbstractChannelInitializer extends ChannelInitializer<SocketChannel> {

    private ChannelPipeline pipeline;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        this.pipeline = ch.pipeline();
        doInitChannel(ch);
    }

    /**
     * 初始化Channel具体方法
     * 
     * @param ch
     * @throws Exception
     */
    abstract protected void doInitChannel(SocketChannel ch) throws Exception;

    /**
     * 返回当前绑定的 {@link io.netty.channel.ChannelPipeline}
     * 
     * @return
     */
    protected ChannelPipeline pipeline() {
        return this.pipeline;
    }

    /**
     * 在 {@link AbstractChannelInitializer#pipeline()} 后添加指定的
     * {@link io.netty.channel.ChannelHandler}
     * 
     * @param handlerClassnames
     */
    protected void addChannelHandlers(List<String> handlerClassnames) {
        handlerClassnames.stream().map(this::getChannelHandlerObject).forEach(pipeline::addLast);
    }

    /**
     * 构造 {@link io.netty.channel.ChannelHandler}
     * 
     * @param qualifiedClassName
     * @return
     */
    abstract protected ChannelHandler getChannelHandlerObject(String qualifiedClassName);

}