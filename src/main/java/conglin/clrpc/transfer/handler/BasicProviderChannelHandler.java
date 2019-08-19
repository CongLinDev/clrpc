package conglin.clrpc.transfer.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.receiver.RequestReceiver;
import conglin.clrpc.transfer.sender.ResponseSender;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class BasicProviderChannelHandler extends SimpleChannelInboundHandler<BasicRequest> {

    private static final Logger log = LoggerFactory.getLogger(BasicProviderChannelHandler.class);

    private final ResponseSender sender;
    private final RequestReceiver receiver;

    public BasicProviderChannelHandler(ResponseSender sender, RequestReceiver receiver) {
        super();
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicRequest msg) throws Exception {
        receiver.getExecutorService().submit(()->{
            BasicResponse response = receiver.handleRequest(msg);
            sender.sendResponse(ctx.channel(), response);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.close();
    }

}