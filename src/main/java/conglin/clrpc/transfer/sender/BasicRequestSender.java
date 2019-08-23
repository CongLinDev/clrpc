package conglin.clrpc.transfer.sender;

import java.net.InetSocketAddress;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchProviderException;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;

/**
 * 基本的请求发送器
 * 针对任意请求生成一个随机的请求ID
 */
public class BasicRequestSender implements RequestSender {
    
    private static final Logger log = LoggerFactory.getLogger(BasicRequestSender.class);

    protected ConsumerServiceHandler serviceHandler;
    protected BiFunction<String, Object, Channel> providerChooser;

    @Override
    public void run() {
        // do nothing
    }

    @Override
    public void init(ConsumerServiceHandler serviceHandler, BiFunction<String, Object, Channel> providerChooser) {
        this.serviceHandler = serviceHandler;
        this.providerChooser = providerChooser;
    }

    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        // BasicRequestSender 发送器使用 UUID 生成 requestID
        BasicFuture future = generateFuture(this::generateRequestId, request);
        String addr = sendRequestCore(request);
        future.setRemoteAddress(addr);
        return future;
    }
    

    @Override
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchProviderException {
        // BasicRequestSender 发送器使用 UUID 生成 requestID
        RpcFuture future = generateFuture(this::generateRequestId, request);
        sendRequestCore(remoteAddress, request);
        return future;
    }

    @Override
    public void resendRequest(String remoteAddress, BasicRequest request) throws NoSuchProviderException {
        sendRequestCore(remoteAddress, request);
    }

    /**
     * 生成请求ID 后
     * 生成 RPCFuture 并且将其保存
     * @param requestIdGenerator 请求ID生成器。输入为服务名，输出为生成的ID。
     * @param request
     * @return
     */
    protected BasicFuture generateFuture(Function<String, Long> requestIdGenerator, BasicRequest request){
        String serviceName = request.getServiceName();
        Long requestId = requestIdGenerator.apply(serviceName);
        request.setRequestId(requestId);

        BasicFuture future = new BasicFuture(this, request);
        serviceHandler.putFuture(request.getRequestId(), future);
        return future;
    }

    /**
     * 发送请求核心函数
     * @param requestIdGenerator 请求ID生成器。输入为服务名，输出为生成的ID。
     * @param request
     */
    protected String sendRequestCore(BasicRequest request){
        String serviceName = request.getServiceName();
        Long requestId = request.getRequestId();

        // 此处保证 channel 非空，故不需要检查
        Channel channel = providerChooser.apply(serviceName, requestId);

        channel.writeAndFlush(request).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Send request Id = " + requestId);
        return ((InetSocketAddress)(channel.remoteAddress())).toString();
    }

    /**
     * 发送请求核心函数
     * @param address 指定地址发送
     * @param request
     * @throws NoSuchProviderException
     */
    protected void sendRequestCore(String address, BasicRequest request)
        throws NoSuchProviderException{
        String serviceName = request.getServiceName();
        Long requestId = request.getRequestId();

        Channel channel = providerChooser.apply(serviceName, requestId);
        if(channel == null) throw new NoSuchProviderException(address, request);
        
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