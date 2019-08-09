package conglin.clrpc.transfer.net.sender;

import java.util.UUID;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchServerException;
import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.ClientServiceHandler;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 基本的请求发送器
 * 针对任意请求生成一个随机的请求ID
 */
public class BasicRequestSender implements RequestSender {
    
    private static final Logger log = LoggerFactory.getLogger(BasicRequestSender.class);

    protected ClientTransfer clientTransfer;
    protected ClientServiceHandler serviceHandler;

    @Override
    public void run() {
        // do nothing
    }

    @Override
    public void init(ClientServiceHandler serviceHandler, ClientTransfer clientTransfer) {
        this.serviceHandler = serviceHandler;
        this.clientTransfer = clientTransfer;
    }

    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        // BasicRequestSender 发送器使用 UUID 生成 requestID
        RpcFuture future = generateFuture(this::generateRequestId, request);
        sendRequestCore(request);
        return future;
    }
    

    @Override
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchServerException {
        // BasicRequestSender 发送器使用 UUID 生成 requestID
        RpcFuture future = generateFuture(this::generateRequestId, request);
        sendRequestCore(remoteAddress, request);
        return future;
    }

    /**
     * 生成请求ID 后
     * 生成 RPCFuture 并且将其保存
     * @param requestIdGenerator 请求ID生成器。输入为服务名，输出为生成的ID。
     * @param request
     * @return
     */
    protected RpcFuture generateFuture(Function<String, Long> requestIdGenerator, BasicRequest request){
        String serviceName = request.getServiceName();
        Long requestId = requestIdGenerator.apply(serviceName);
        request.setRequestId(requestId);

        RpcFuture future = new RpcFuture(request);
        serviceHandler.putFuture(request.getRequestId(), future);
        return future;
    }

    /**
     * 发送请求核心函数
     * @param requestIdGenerator 请求ID生成器。输入为服务名，输出为生成的ID。
     * @param request
     */
    protected void sendRequestCore(BasicRequest request){
        String serviceName = request.getServiceName();
        Long requestId = request.getRequestId();

        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(serviceName, requestId);
        Channel channel = channelHandler.getChannel();
        channel.writeAndFlush(request).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Send request Id = " + requestId);
    }

    /**
     * 发送请求核心函数
     * @param address 指定地址发送
     * @param request
     * @throws NoSuchServerException
     */
    protected void sendRequestCore(String address, BasicRequest request)
        throws NoSuchServerException{
        String serviceName = request.getServiceName();
        Long requestId = request.getRequestId();

        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(serviceName, address);
        if(channelHandler == null) throw new NoSuchServerException(address, request);

        Channel channel = channelHandler.getChannel();
        channel.writeAndFlush(request).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Send request Id = " + requestId);
    }

    /**
     * 生成 RequestID
     * 使用UUID随机分配
     * @param serviceName
     * @return
     */
    protected Long generateRequestId(String serviceName){
        return UUID.randomUUID().getLeastSignificantBits();
    }

    @Override
    public void stop() {
        // do nothing
    }
}