package conglin.clrpc.transfer.net.sender;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import io.netty.channel.Channel;

/**
 * 基本的请求发送器
 * 针对任意请求生成一个随机的请求ID
 */
public class BasicRequestSender implements RequestSender {

    protected ClientTransfer clientTransfer;

    @Override
    public void run() {
        // do nothing
    }

    @Override
    public void init(ClientTransfer clientTransfer) {
        this.clientTransfer = clientTransfer;
    }

    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        // BasicRequestSender 发送器使用 UUID 生成 requestID
        String requestId = generateRequestId(null);
        request.setRequestId(requestId);

        RpcFuture future = new RpcFuture(request);
        clientTransfer.saveFuture(requestId, future);
        sendRequestCore(request);
        return future;
    }
    
    /**
     * 发送请求核心函数
     * @param request
     */
    protected void sendRequestCore(BasicRequest request){
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(request.getServiceName());
        Channel channel = channelHandler.getChannel();
        channel.writeAndFlush(request);
    }

    /**
     * 生成 RequestID
     * 使用UUID随机分配
     * @param serviceName
     * @return
     */
    protected String generateRequestId(String serviceName){
        return UUID.randomUUID().toString();
    }

    @Override
    public void stop() {
        // do nothing
    }
}