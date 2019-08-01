package conglin.clrpc.transfer.net.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.receiver.RequestReceiver;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class BasicServerChannelHandler extends SimpleChannelInboundHandler<BasicRequest> {

    private static final Logger log = LoggerFactory.getLogger(BasicClientChannelHandler.class);

    private final RequestReceiver requestReceiver;

    public BasicServerChannelHandler(RequestReceiver requestReceiver) {
        super();
        this.requestReceiver = requestReceiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicRequest msg) throws Exception {
        requestReceiver.handleRequest(ctx.channel(), msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.close();
    }

}