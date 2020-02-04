package conglin.clrpc.common.loadbalance;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import conglin.clrpc.common.Pair;

/**
 * 该接口用作负载均衡 适合 {一个 type 对应多个 key-value 对} 组的负载均衡
 * 
 * @param <T> type
 * @param <K> key
 * @param <V> value
 */
public interface LoadBalancer<T, K, V> {

    /**
     * 使用集合更新数据
     * 
     * @param type
     * @param data 其中key为连接的ip地址，value为 服务器的元信息
     */
    void update(T type, Collection<Pair<K, String>> data);

    /**
     * 根据寻找第一个可用对象
     * 
     * @param type
     * @return
     */
    default V get(T type) {
        return get(type, 0);
    }

    /**
     * 根据指定算法返回对象
     * 
     * @param type
     * @param random
     * @return
     */
    V get(T type, int random);

    /**
     * 根据条件返回指定对象
     * 
     * @param type
     * @param key
     * @return
     */
    V get(T type, K key);

    /**
     * 根据条件返回指定对象
     * 
     * @param type
     * @param predicate
     * @return
     */
    V get(T type, Predicate<V> predicate);

    /**
     * 是否存在该类型
     * 
     * @param type
     * @return
     */
    boolean hasType(T type);

    /**
     * 给定类型下是否有可用的对象
     * 
     * @param type
     * @return
     */
    default boolean hasNext(T type) {
        return hasType(type) && get(type) != null;
    }

    /**
     * 对所有节点做出某个操作
     * 
     * @param consumer
     */
    void forEach(Consumer<V> consumer);

    /**
     * 对某个类型的所有节点做出某个操作
     * 
     * @param type
     * @param consumer
     */
    void forEach(T type, Consumer<V> consumer);

    /**
     * 清空容器内所有数据
     */
    void clear();
}