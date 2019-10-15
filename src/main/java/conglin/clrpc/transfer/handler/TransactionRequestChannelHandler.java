package conglin.clrpc.transfer.handler;

import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.transfer.message.BasicResponse;
import conglin.clrpc.transfer.message.TransactionRequest;
import conglin.clrpc.transfer.receiver.RequestReceiver;
import conglin.clrpc.transfer.sender.ResponseSender;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TransactionRequestChannelHandler extends SimpleChannelInboundHandler<TransactionRequest>{

    private static final Logger log = LoggerFactory.getLogger(TransactionRequestChannelHandler.class);
    
    private final ResponseSender sender;
    private final RequestReceiver receiver;

    public TransactionRequestChannelHandler(ResponseSender sender, RequestReceiver receiver) {
        super();
        this.sender = sender;
        this.receiver = receiver;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TransactionRequest msg) throws Exception {
        executorService().submit(()->{
            BasicResponse response = receiver.handleRequest(msg);
            sender.sendResponse(ctx.channel(), response);
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        log.error(cause.getMessage());
        ctx.close();
    }

    protected ExecutorService executorService(){
        return receiver.getExecutorService();
    }
}