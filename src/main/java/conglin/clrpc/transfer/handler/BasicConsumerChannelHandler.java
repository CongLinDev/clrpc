package conglin.clrpc.transfer.handler;

import java.net.InetSocketAddress;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.receiver.ResponseReceiver;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class BasicConsumerChannelHandler 
        extends SimpleChannelInboundHandler<BasicResponse> implements Comparable<String>{

    private static final Logger log = LoggerFactory.getLogger(BasicConsumerChannelHandler.class);

    private final ResponseReceiver receiver;

    private Channel channel;

    public BasicConsumerChannelHandler(ResponseReceiver receiver){
        this.receiver = receiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicResponse msg) throws Exception {
        receiver.handleResponse(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        cause.printStackTrace();
        ctx.close();
    }

    
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        this.channel = ctx.channel();
    }

    public Channel getChannel(){
        return channel;
    }

    public void close() {
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public int compareTo(String o) {
        return ((InetSocketAddress)channel.remoteAddress()).toString().compareTo(o);
    }
}