package conglin.clrpc.service;

import java.lang.reflect.Proxy;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;

import javax.security.auth.DestroyFailedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.config.PropertyConfigurer;
import conglin.clrpc.common.identifier.IdentifierGenerator;
import conglin.clrpc.service.context.ConsumerContext;
import conglin.clrpc.service.discovery.BasicServiceDiscovery;
import conglin.clrpc.service.discovery.ServiceDiscovery;
import conglin.clrpc.service.future.FuturesHolder;
import conglin.clrpc.service.future.RpcFuture;
import conglin.clrpc.service.proxy.BasicObjectProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.service.proxy.TransactionProxy;
import conglin.clrpc.service.proxy.ZooKeeperTransactionProxy;

public class ConsumerServiceHandler extends AbstractServiceHandler implements FuturesHolder<Long> {

    private static final Logger log = LoggerFactory.getLogger(ConsumerServiceHandler.class);

    private final Map<Long, RpcFuture> rpcFutures;

    private final ServiceDiscovery serviceDiscovery;

    private String LOCAL_ADDRESS;

    private ConsumerContext context;

    // ID生成器
    private IdentifierGenerator identifierGenerator;

    public ConsumerServiceHandler(PropertyConfigurer configurer) {
        super(configurer);
        rpcFutures = new ConcurrentHashMap<>();
        serviceDiscovery = new BasicServiceDiscovery(configurer);
    }

    /**
     * 获取同步服务代理
     * 
     * @param <T>
     * @param interfaceClass
     * @param serviceName
     * @return
     */
    @SuppressWarnings("unchecked")
    public <T> T getPrxoy(Class<T> interfaceClass, String serviceName) {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(), new Class<?>[] { interfaceClass },
                new BasicObjectProxy(serviceName, context.getRequestSender(), identifierGenerator));
    }

    /**
     * 获取异步服务代理
     * 
     * @param serviceName
     * @return
     */
    public ObjectProxy getPrxoy(String serviceName) {
        return new BasicObjectProxy(serviceName, context.getRequestSender(), identifierGenerator);
    }

    /**
     * 获取事务服务代理
     * 
     * @return
     */
    public TransactionProxy getTransactionProxy() {
        return new ZooKeeperTransactionProxy(context.getRequestSender(), identifierGenerator,
                context.getPropertyConfigurer());
    }

    /**
     * 启动 获得请求发送器，用于检查超时Future 重发请求
     * 
     * @param context
     */
    public void start(ConsumerContext context) {
        this.context = context;
        LOCAL_ADDRESS = context.getLocalAddress();
        context.setExecutorService(getExecutorService());
        context.setFuturesHolder(this);

        identifierGenerator = context.getIdentifierGenerator();
        checkFuture();
    }

    /**
     * 停止
     */
    public void stop() {
        waitForUncompleteFuture();
        
        if(!super.isDestroyed()){
            try{
                super.destroy();
            }catch(DestroyFailedException e){
                log.error(e.getMessage());
            }
        }

        if(!serviceDiscovery.isDestroyed()){
            try {
                serviceDiscovery.destroy();
            } catch (DestroyFailedException e) {
                log.error(e.getMessage());
            }
        }

        if(!identifierGenerator.isDestroyed()){
            try {
                identifierGenerator.destroy();
            } catch (DestroyFailedException e) {
                log.error(e.getMessage());
            }
        }
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

    @Override
    public void putFuture(Long key, RpcFuture rpcFuture){
        rpcFutures.put(key, rpcFuture);
    }

    @Override
    public RpcFuture getFuture(Long key){
        return rpcFutures.get(key);
    }

    @Override
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
                    if(f.isPending() && f.timeout()){
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