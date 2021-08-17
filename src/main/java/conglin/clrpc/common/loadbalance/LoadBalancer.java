package conglin.clrpc.common.loadbalance;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

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
    void update(T type, Collection<K> data);

    /**
     * 根据type寻找第一个可用对象
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
    default V getKey(T type, K key) {
        if(key == null) return get(type);
        return getKey(type, key::equals);
    }

    /**
     * 根据条件返回指定对象
     *
     * @param type
     * @param predicate
     * @return
     */
    V getKey(T type, Predicate<K> predicate);

    /**
     * 根据条件返回指定对象
     * 
     * @param type
     * @param predicate
     * @return
     */
    V getValue(T type, Predicate<V> predicate);

    /**
     * 获取所有类型
     * 
     * @return
     */
    Collection<T> allTypes();

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
     * 对所有节点做出某个操作
     * 
     * @param <R>
     * @param function
     * @return
     */
    <R> Collection<R> apply(Function<V, R> function);

    /**
     * 对某个类型的所有节点做出某个操作
     * 
     * @param type
     * @param consumer
     */
    void forEach(T type, Consumer<V> consumer);

    /**
     * 对所有节点做出某个操作
     * 
     * @param <R>
     * @param function
     * @return
     */
    <R> Collection<R> apply(T type, Function<V, R> function);

    /**
     * 清空容器内所有数据
     */
    void clear();
}