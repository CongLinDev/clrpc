package conglin.clrpc.service.future;

import java.util.Iterator;

public interface FuturesHolder<K> {
    /**
     * 加入 Future
     * 
     * @param key
     * @param rpcFuture
     */
    void putFuture(K key, RpcFuture rpcFuture);

    /***
     * 获取 Future
     * 
     * @param key
     * @return
     */
    RpcFuture getFuture(K key);

    /**
     * 移除 Future
     * 
     * @param key
     * @return
     */
    RpcFuture removeFuture(K key);

    /**
     * 获取迭代器
     * 
     * @return
     */
    Iterator<RpcFuture> iterator();

    /**
     * 等待所有未完成的 {@link conglin.clrpc.service.future.RpcFuture} 用于优雅的关闭
     */
    void waitForUncompleteFuture();
}