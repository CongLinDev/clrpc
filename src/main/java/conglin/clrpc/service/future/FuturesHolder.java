package conglin.clrpc.service.future;

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
}