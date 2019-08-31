package conglin.clrpc.service;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.service.discovery.BasicServiceDiscovery;
import conglin.clrpc.service.discovery.ServiceDiscovery;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.proxy.BasicObjectProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.sender.RequestSender;


public class ConsumerServiceHandler extends AbstractServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ConsumerServiceHandler.class);

    private final Map<Long, RpcFuture> rpcFutures;

    private ServiceDiscovery serviceDiscovery;

    private final String LOCAL_ADDRESS;

    public ConsumerServiceHandler(String localAddress){
        super();
        this.LOCAL_ADDRESS = localAddress;
        rpcFutures = new ConcurrentHashMap<>();
        serviceDiscovery = new BasicServiceDiscovery();
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
    public <T> T getPrxoy(Class<T> interfaceClass, String serviceName, RequestSender sender){
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
    public ObjectProxy getPrxoy(String serviceName, RequestSender sender){
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

    @Override
    public void stop(){
        waitForUncompleteFuture();
        super.stop();
        serviceDiscovery.stop();
    }


    /**
     * 发现服务
     * 共两步 1. 注册消费者
     *       2. 发现服务并进行连接服务提供者
     * @param serviceName
     * @param updateMethod
     */
    public void findService(String serviceName, BiConsumer<String, List<String>> updateMethod){
        serviceDiscovery.registerConsumer(serviceName, LOCAL_ADDRESS);
        serviceDiscovery.discover(serviceName, updateMethod);
    }
    

    /**
     * 发现服务
     * 共两步 1. 注册消费者
     *       2. 发现服务并进行连接服务提供者
     * @param interfaceClass
     * @param updateMethod
     */
    public void findService(Class<?> interfaceClass, BiConsumer<String, List<String>> updateMethod){
        findService(interfaceClass.getSimpleName(), updateMethod);
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
                        log.warn("Service response(requestId=" + f.identifier() + ") is too slow. Retry...");
                    }
                }
            }
        }, MAX_DELARY);
    }

    /**
     * 等待所有未完成的 {@link conglin.clrpc.service.future.RpcFuture}
     * 用于优雅的关闭 {@link conglin.clrpc.service.ConsumerServiceHandler}
     */
    private void waitForUncompleteFuture(){
        while(rpcFutures.size() != 0){
            try {
                log.info("Waiting uncomplete futures for 500 ms.");
                Thread.sleep(500);
            } catch (InterruptedException e) {
                log.error(e.getMessage());
            }
        }
    }
}