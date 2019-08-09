package conglin.clrpc.service.loadbalance;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public interface LoadBalanceHandler<K, V> {

    /**
     * 使用String列表更新数据
     * @param key
     * @param data
     * @param start 添加V的工作
     * @param stop 移除的V的收尾工作
     */
    void update(K key, List<String> data, Function<String, V> start, Consumer<V> stop);

    /**
     * 根据指定算法返回对象
     * @param key
     * @param random
     * @return
     */
    V get(K key, Object random);

    /**
     * 根据条件返回指定对象
     * @param key
     * @param string
     * @return
     */
    V get(K key, String string);

    /**
     * 根据条件返回指定对象
     * @param key
     * @param predicate
     * @return
     */
    V get(K key, Predicate<V> predicate);


    boolean hasNext(K key);

    /**
     * 对所有节点做出某个操作
     * @param consumer
     */
    void forEach(Consumer<V> consumer);
}