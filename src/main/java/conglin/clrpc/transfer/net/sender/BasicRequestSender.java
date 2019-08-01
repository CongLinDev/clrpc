package conglin.clrpc.transfer.net.sender;

import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.transfer.net.ClientTransfer;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import io.netty.channel.Channel;

public class BasicRequestSender implements RequestSender {

    private static final Logger log = LoggerFactory.getLogger(BasicRequestSender.class);

    private ClientTransfer clientTransfer;

    // private final int REQUEST_QUEUE_MAX_SIZE;
    // private final int REQUEST_QUEUE_TIME_THRESHOLD;
    // private final Map<String, Queue<BasicRequest>> allKindsOfRequests;

    public BasicRequestSender(){
        // allKindsOfRequests = new ConcurrentHashMap<>();
        // REQUEST_QUEUE_MAX_SIZE = ConfigParser.getOrDefault("service.request-queue.max-size", 20);
        // REQUEST_QUEUE_TIME_THRESHOLD = ConfigParser.getOrDefault("service.request-queue.time-threshold", 100);
    }

    @Override
    public void run() {
        log.debug("conglin.clrpc.transfer.net.sender.BasicRequestSender#run() do nothing in background...");
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

    @Override
    public void init(ClientTransfer clientTransfer) {
        this.clientTransfer = clientTransfer;
    }

    @Override
	public RpcFuture sendRequest(BasicRequest request) {
        // BasicRequestSender 发送器使用 UUID 生成 requestID
        String requestId = UUID.randomUUID().toString();
        request.setRequestId(requestId);

        RpcFuture future = new RpcFuture(request);
        clientTransfer.saveFuture(requestId, future);
        sendRequestCore(request);
        return future;
    }
    
    private void sendRequestCore(BasicRequest request){
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(request.getServiceName());
        Channel channel = channelHandler.getChannel();
        channel.writeAndFlush(request);
        // CountDownLatch countDownLatch = new CountDownLatch(1);
        // channel.writeAndFlush(request).addListener(new ChannelFutureListener(){
        //     @Override
        //     public void operationComplete(ChannelFuture future) throws Exception {
        //         countDownLatch.countDown();
        //     }
        // });
        // try{
        //     countDownLatch.await();
        // }catch(InterruptedException e){
        //     log.error(e.getMessage());
        // }
    }
    
}