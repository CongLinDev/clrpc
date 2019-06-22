package conglin.clrpc.service;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.proxy.BasicObjectProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.ClientTransfer;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientServiceHandler extends ServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceHandler.class);

    private Map<String, RpcFuture> rpcFutures;

    private ClientTransfer clientTransfer;

    public ClientServiceHandler(){
        rpcFutures = new ConcurrentHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> interfaceClass){
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            new BasicObjectProxy<T>(interfaceClass, this, clientTransfer));
    }

    public <T> ObjectProxy getAsynchronousService(Class<T> interfaceClass){
        return new BasicObjectProxy<T>(interfaceClass, this, clientTransfer);
    }

    public void start(ClientTransfer clientTransfer){
        this.clientTransfer = clientTransfer;
    }
    




    


    /**
     * 对于每个 BasicRequest 请求，都会有一个 RpcFuture 等待一个 BasicResponse 响应
     * 这些未到达客户端的 BasicResponse 响应 换言之即为 RpcFuture
     * 被保存在 ClientServiceHandler 中的一个 list 中
     * 以下代码用于 RpcFuture 的管理和维护
     */



    public void putFuture(String requestId, RpcFuture rpcFuture){
        rpcFutures.put(requestId, rpcFuture);
    }

    public RpcFuture getFuture(String requestId){
        return rpcFutures.get(requestId);
    }

    public RpcFuture removeFuture(String requestId){
        return rpcFutures.remove(requestId);
    }







    /**
     * 用于发送RPC请求
     * @param request 请求
     * @param channel 发送的通道
     * @return
     */
    public RpcFuture sendRequest(BasicRequest request, Channel channel){
        final CountDownLatch countDownLatch = new CountDownLatch(1);

        RpcFuture future = new RpcFuture(request);
        rpcFutures.put(request.getRequestId(), future);
        channel.writeAndFlush(request).addListener(new ChannelFutureListener(){
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                countDownLatch.countDown();
            }
        });
        try{
            countDownLatch.await();
        }catch(InterruptedException e){
            log.error("", e);
        }
        return future;
    }
}