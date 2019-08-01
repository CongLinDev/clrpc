package conglin.clrpc.service;

import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.util.concurrent.RpcFuture;
import conglin.clrpc.service.proxy.BasicObjectProxy;
import conglin.clrpc.service.proxy.ObjectProxy;
import conglin.clrpc.transfer.net.sender.RequestSender;


public class ClientServiceHandler extends AbstractServiceHandler {

    private static final Logger log = LoggerFactory.getLogger(ClientServiceHandler.class);

    private final Map<String, RpcFuture> rpcFutures;

    public ClientServiceHandler(){
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
    public <T> T getService(Class<T> interfaceClass, String serviceName, RequestSender sender){
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
    public ObjectProxy getAsynchronousService(String serviceName, RequestSender sender){
        return new BasicObjectProxy(serviceName, sender);
    }

    /**
     * 启动
     */
    public void start(){
        log.debug("conglin.clrpc.service.ClientServiceHandler#start() do nothing...");
    }
    




    


    /**
     * 对于每个 BasicRequest 请求，都会有一个 RpcFuture 等待一个 BasicResponse 响应
     * 这些未到达客户端的 BasicResponse 响应 换言之即为 RpcFuture
     * 被保存在 ClientServiceHandler 中的一个 list 中
     * 以下代码用于 RpcFuture 的管理和维护
     */

    /**
     * 加入 Future
     * @param key
     * @param rpcFuture
     */
    public void putFuture(String key, RpcFuture rpcFuture){
        rpcFutures.put(key, rpcFuture);
    }

    /***
     * 获取 Future
     * @param key
     * @return
     */
    public RpcFuture getFuture(String key){
        return rpcFutures.get(key);
    }

    /**
     * 移除 Future
     * @param key
     * @return
     */
    public RpcFuture removeFuture(String key){
        return rpcFutures.remove(key);
    }
}