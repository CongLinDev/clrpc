package conglin.clrpc.service.future;

import java.util.Iterator;

public interface FutureHolder<K> {
    /**
     * 加入 Future
     * 
     * @param key
     * @param futures
     */
    void putFuture(K key, InvocationFuture futures);

    /***
     * 获取 Future
     * 
     * @param key
     * @return
     */
    InvocationFuture getFuture(K key);

    /**
     * 移除 Future
     * 
     * @param key
     * @return
     */
    InvocationFuture removeFuture(K key);

    /**
     * 获取迭代器
     * 
     * @return
     */
    Iterator<InvocationFuture> iterator();

    /**
     * 等待所有未完成的 {@link conglin.clrpc.service.future.InvocationFuture} 用于优雅的关闭
     */
    void waitForUncompletedFuture();
}