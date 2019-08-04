package conglin.clrpc.transfer.net.sender;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.ConfigParser;
import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.transfer.net.handler.BasicClientChannelHandler;
import conglin.clrpc.transfer.net.message.BasicRequest;
import io.netty.channel.Channel;

public class QueueRequestSender extends BasicRequestSender {
    private static final Logger log = LoggerFactory.getLogger(QueueRequestSender.class);

    private final int REQUEST_QUEUE_MAX_SIZE;
    private final int REQUEST_QUEUE_TIME_THRESHOLD;
    private final Map<String, Queue<BasicRequest>> allKindsOfRequests;

    public QueueRequestSender(){
        allKindsOfRequests = new ConcurrentHashMap<>();
        REQUEST_QUEUE_MAX_SIZE = ConfigParser.getOrDefault("service.request-queue.max-size", 20);
        REQUEST_QUEUE_TIME_THRESHOLD = ConfigParser.getOrDefault("service.request-queue.time-threshold", 100);
    }

    @Override
    public void run() {
        //轮询线程，负责发送请求
        super.serviceHandler.execute(()->{
            while(!Thread.currentThread().isInterrupted()){
                for(Queue<BasicRequest> requests : allKindsOfRequests.values()){
                    if(requests.size() != 0){
                        sendRequestCore(requests);
                    }
                }
                try {
                    Thread.sleep(REQUEST_QUEUE_TIME_THRESHOLD);
                    log.info("sleep.....");
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    @Override
    public RpcFuture sendRequest(BasicRequest request) {

        String requestId = generateRequestId(null);
        request.setRequestId(requestId);

        RpcFuture future = new RpcFuture(request);
        serviceHandler.putFuture(requestId, future);

        Queue<BasicRequest> queue = putAndCheckCacheQueue(request);
        if(queue != null)
            sendRequestCore(queue);

        return future;
    }

    protected void sendRequestCore(Queue<BasicRequest> requests){
        String serviceName = requests.peek().getServiceName();
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(serviceName);
        Channel channel = channelHandler.getChannel();
        while(!requests.isEmpty()){
            BasicRequest r = requests.poll();
            channel.write(r);
        }
        channel.flush();
    }

    private Queue<BasicRequest> putAndCheckCacheQueue(BasicRequest request){
        Queue<BasicRequest> queue = allKindsOfRequests.getOrDefault(request.getServiceName(), new ConcurrentLinkedQueue<>());
        queue.offer(request);
        allKindsOfRequests.put(request.getServiceName(), queue);

        if(queue.size() >= (REQUEST_QUEUE_MAX_SIZE >> 1)){
            return queue;
        }else{
            return null;
        }
    }

}