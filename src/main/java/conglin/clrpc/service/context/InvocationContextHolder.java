package conglin.clrpc.service.context;

import java.util.Iterator;

public interface InvocationContextHolder<K> {
    /**
     * 加入 context
     * 
     * @param key
     * @param context
     */
    void put(K key, InvocationContext context);

    /***
     * 获取 context
     * 
     * @param key
     * @return
     */
    InvocationContext get(K key);

    /**
     * 移除 context
     * 
     * @param key
     * @return
     */
    InvocationContext remove(K key);

    /**
     * 获取迭代器
     * 
     * @return
     */
    Iterator<InvocationContext> iterator();

    /**
     * 等待所有未完成的 {@link InvocationContext} 用于优雅的关闭
     */
    void waitForUncompletedInvocation();
}
