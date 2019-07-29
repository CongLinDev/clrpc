package conglin.clrpc.service;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.proxy.BasicObjectProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;


public class ClientServiceHandler extends AbstractServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceHandler.class);

    private final Map<String, RpcFuture> rpcFutures;

    // private final int REQUEST_QUEUE_MAX_SIZE;
    // private final int REQUEST_QUEUE_TIME_THRESHOLD;
    // private final Map<String, Queue<BasicRequest>> allKindsOfRequests;

    private ClientTransfer clientTransfer;

    public ClientServiceHandler(){
        super();
        rpcFutures = new ConcurrentHashMap<>();
        // allKindsOfRequests = new ConcurrentHashMap<>();
        // REQUEST_QUEUE_MAX_SIZE = ConfigParser.getOrDefault("service.request-queue.max-size", 20);
        // REQUEST_QUEUE_TIME_THRESHOLD = ConfigParser.getOrDefault("service.request-queue.time-threshold", 100);
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
        //轮询线程，负责发送请求
        // super.execute(()->{
        //     while(!Thread.interrupted()){
        //         int count = 0;//标志位，记录遍历过的空队列个数
        //         for(Queue<BasicRequest> requests : allKindsOfRequests.values()){
        //             if(requests == null || requests.size() == 0){
        //                 count++;
        //             }else{
        //                 count = 0;
        //                 String serviceName = requests.peek().getServiceName();
        //                 sendRequestCore(serviceName, requests);
        //             }
        //         }
        //         try {
        //             Thread.sleep(REQUEST_QUEUE_TIME_THRESHOLD);
        //             log.info("sleep.....");
        //         } catch (InterruptedException e) {
        //             log.error(e.getMessage());
        //         }
        //         if(count != 0 && count >= (allKindsOfRequests.size() >> 1))
        //             break;
        //     }
        // });
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
        RpcFuture future = new RpcFuture(request);
        rpcFutures.put(request.getRequestId(), future);
        sendRequestCore(request);
        return future;
    }

    /**
     * 发送请求核心方法
     * 注意：此方法未检查队列是否为 null和队列的大小
     * @param serviceName
     * @param requests
     */
    private void sendRequestCore(BasicRequest request){
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(request.getServiceName());
        Channel channel = channelHandler.getChannel();

        CountDownLatch countDownLatch = new CountDownLatch(1);
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
    }
}