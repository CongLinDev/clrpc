package conglin.clrpc.service;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.proxy.BasicObjectProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.sender.RequestSender;


public class ConsumerServiceHandler extends AbstractServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ConsumerServiceHandler.class);

    private final Map<Long, RpcFuture> rpcFutures;

    public ConsumerServiceHandler(){
        super();
        rpcFutures = new ConcurrentHashMap<>();
    }

    /**
     * 获取同步服务代理
     * @param <T>
     * @param interfaceClass
     * @param serviceName
     * @param sender
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T subscribeService(Class<T> interfaceClass, String serviceName, RequestSender sender){
        return (T) Proxy.newProxyInstance(
            interfaceClass.getClassLoader(),
            new Class<?>[]{interfaceClass},
            new BasicObjectProxy(serviceName, sender));
    }

    /**
     * 获取异步服务代理
     * @param serviceName
     * @param sender
     * @return
     */
    public ObjectProxy subscribeServiceAsync(String serviceName, RequestSender sender){
        return new BasicObjectProxy(serviceName, sender);
    }

    /**
     * 启动
     * 获得请求发送器，用于检查超时Future
     * 重发请求
     * @param sender
     */
    public void start(){
        checkFuture();
    }
    




    


    /**
     * 对于每个 BasicRequest 请求，都会有一个 RpcFuture 等待一个 BasicResponse 响应
     * 这些未到达客户端的 BasicResponse 响应 换言之即为 RpcFuture
     * 被保存在 ConsumerServiceHandler 中的一个 list 中
     * 以下代码用于 RpcFuture 的管理和维护
     */

    /**
     * 加入 Future
     * @param key
     * @param rpcFuture
     */
    public void putFuture(Long key, RpcFuture rpcFuture){
        rpcFutures.put(key, rpcFuture);
    }

    /***
     * 获取 Future
     * @param key
     * @return
     */
    public RpcFuture getFuture(Long key){
        return rpcFutures.get(key);
    }

    /**
     * 移除 Future
     * @param key
     * @return
     */
    public RpcFuture removeFuture(Long key){
        return rpcFutures.remove(key);
    }

    /**
     * 轮询线程，检查超时 RpcFuture
     * 超时重试
     * @param sender
     */
    private void checkFuture(){
        final long MAX_DELARY = 3000; //最大延迟为3000 ms
        new Timer("check-uncomplete-future", true).schedule(new TimerTask(){
        
            @Override
            public void run() {
                Iterator<RpcFuture> iterator = rpcFutures.values().iterator();
                while(iterator.hasNext()){
                    RpcFuture f = iterator.next();
                    if(!f.isDone() && f.timeout()){
                        f.retry();
                        log.warn("Service response time is too slow. Request ID = " + f.identifier() + ". Retry...");
                    }
                }
            }
        }, MAX_DELARY);
    }
}