package conglin.clrpc.transfer.sender;

import java.net.InetSocketAddress;
import java.util.function.BiFunction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.exception.NoSuchProviderException;
import conglin.clrpc.service.ConsumerServiceHandler;
import conglin.clrpc.service.cache.CacheManager;
import conglin.clrpc.service.future.BasicFuture;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.transfer.message.BasicRequest;
import conglin.clrpc.transfer.message.BasicResponse;
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

    protected CacheManager<BasicRequest, BasicResponse> cacheManager;

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
    public void bindCachePool(CacheManager<BasicRequest, BasicResponse> cacheManager){
        this.cacheManager = cacheManager;
    }

    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        BasicFuture future = new BasicFuture(this, request);
        if(!putFuture(request, future)) return future;

        String addr = doSendRequest(request);
        future.setRemoteAddress(addr);
        return future;
    }
    

    @Override
    public RpcFuture sendRequest(String remoteAddress, BasicRequest request) throws NoSuchProviderException {
        BasicFuture future = new BasicFuture(this, request);
        if(!putFuture(request, future)) return future;

        doSendRequest(remoteAddress, request);
        return future;
    }

    @Override
    public void resendRequest(String remoteAddress, BasicRequest request) throws NoSuchProviderException {
        doSendRequest(remoteAddress, request);
    }

    /**
     * 保存future（实质为读取缓存）
     * 若能从缓存读取信息则不保存future，返回 false
     * @param request
     * @param future
     * @return
     */
    protected boolean putFuture(BasicRequest request, RpcFuture future){
        BasicResponse responseCache = null;
        if(cacheManager != null &&
            (responseCache = cacheManager.get(request)) != null ){
            log.debug("Fetching cached response. Request id = " + request.getRequestId());
            future.done(responseCache);
            return false;
        }
        serviceHandler.putFuture(future.identifier(), future);
        return true;
    }

    /**
     * 发送请求核心函数
     * @param requestIdGenerator 请求ID生成器。输入为服务名，输出为生成的ID。
     * @param request
     */
    protected String doSendRequest(BasicRequest request){
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
    protected void doSendRequest(String address, BasicRequest request)
        throws NoSuchProviderException{
        String serviceName = request.getServiceName();
        Long requestId = request.getRequestId();

        Channel channel = providerChooser.apply(serviceName, requestId);
        if(channel == null) throw new NoSuchProviderException(address, request);
        
        channel.writeAndFlush(request).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        log.debug("Send request Id = " + requestId);
    }


    @Override
    public void destory() {
        // do nothing
    }
}