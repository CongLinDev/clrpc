package conglin.clrpc.service.loadbalance;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * 该接口用作负载均衡
 * 适合一个 type 对应多个 key-value 的负载均衡
 * @param <T> type
 * @param <K> key
 * @param <V> value
 */
public interface LoadBalancer<T, K, V> {

    /**
     * 使用列表更新数据
     * @param type
     * @param data
     * @param start 添加V的工作
     * @param stop 移除的V的收尾工作
     */
    void update(T type, Collection<K> data, Function<K, V> start, Consumer<V> stop);

    /**
     * 根据指定算法返回对象
     * @param type
     * @param random
     * @return
     */
    V get(T type, int random);

    /**
     * 根据条件返回指定对象
     * @param type
     * @param k
     * @return
     */
    V get(T type, K k);

    /**
     * 根据条件返回指定对象
     * @param type
     * @param predicate
     * @return
     */
    V get(T type, Predicate<V> predicate);

    /**
     * 是否有满足 type 的可用的对象
     * @param type
     * @return
     */
    boolean hasNext(T type);

    /**
     * 对所有节点做出某个操作
     * @param consumer
     */
    void forEach(Consumer<V> consumer);

    /**
     * 清空容器内所有数据
     */
    void clear();
}