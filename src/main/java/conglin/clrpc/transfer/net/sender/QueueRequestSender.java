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
import io.netty.channel.ChannelFutureListener;

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
                        sendRequestQueue(requests);
                    }
                }
                try {
                    // 队列时间阈值发送条件
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

        Long requestId = generateRequestId(null);
        request.setRequestId(requestId);

        Queue<BasicRequest> queue = putAndCheckCacheQueue(request);
        if(queue != null)
            sendRequestQueue(queue);

        return generateFuture(request);
    }

    protected void sendRequestQueue(Queue<BasicRequest> requests){
        String serviceName = requests.peek().getServiceName();
        BasicClientChannelHandler channelHandler = clientTransfer.chooseChannelHandler(serviceName);
        Channel channel = channelHandler.getChannel();
        while(!requests.isEmpty()){
            BasicRequest r = requests.poll();
            channel.write(r).addListener(ChannelFutureListener.FIRE_EXCEPTION_ON_FAILURE);
        }
        channel.flush();
    }

    /**
     * 将请求加入队列中，并检查队列是否满足发送条件
     * 队列数量发送条件为 queue.size() >= (REQUEST_QUEUE_MAX_SIZE / 2
     * @param request
     * @return
     */
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