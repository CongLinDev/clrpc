package conglin.clrpc.service;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.proxy.BasicObjectProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.net.message.BasicRequest;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientServiceHandler extends AbstractServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceHandler.class);

    private final Map<String, RpcFuture> rpcFutures;

    private ClientTransfer clientTransfer;

    public ClientServiceHandler(){
        super();
        rpcFutures = new ConcurrentHashMap<>();
    }

    /**
     * 获取同步服务代理
     * @param <T>
     * @param interfaceClass
     * @param serviceName
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getService(Class<T> interfaceClass, String serviceName){
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            new BasicObjectProxy(serviceName, this));
    }

    /**
     * 获取异步服务代理
     * @param serviceName
     * @return
     */
    public ObjectProxy getAsynchronousService(String serviceName){
        return new BasicObjectProxy(serviceName, this);
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
     * 发送 RPC请求
     * @param request
     * @return
     */
    public RpcFuture sendRequest(BasicRequest request){
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(request.getServiceName());
        Channel channel = channelHandler.getChannel();

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
            log.error(e.getMessage());
        }
        return future;
    }
}