package conglin.clrpc.transfer.net.handler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchServiceException;
import conglin.clrpc.common.exception.ServiceExecutionException;
import conglin.clrpc.service.ServerServiceHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.message.BasicResponse;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;


public class BasicServerChannelHandler extends SimpleChannelInboundHandler<BasicRequest> {

    private static final Logger log = LoggerFactory.getLogger(BasicClientChannelHandler.class);

    private final ServerServiceHandler serviceHandler;

    public BasicServerChannelHandler(ServerServiceHandler serviceHandler) {
        super();
        this.serviceHandler = serviceHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, BasicRequest msg) throws Exception {
        serviceHandler.submit(new Runnable() {
            @Override
            public void run() {
                log.debug("Receive request " + msg.getRequestId());
                BasicResponse response = BasicResponse.builder().requestId(msg.getRequestId()).build();
                try {
                    Object result = serviceHandler.handleRequest(msg);
                    response.setResult(result);
                } catch (NoSuchServiceException | ServiceExecutionException e) {
                    log.error("Request failed: " + e.getMessage());
                    response.setError(e.getDescription());
                }

                ctx.writeAndFlush(response).addListener(new ChannelFutureListener(){
                    @Override
                    public void operationComplete(ChannelFuture future) throws Exception {
                        log.debug("Send response for request " + msg.getRequestId());
                    }
                });
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        log.error(cause.getMessage());
        ctx.close();
    }

}