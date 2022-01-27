package conglin.clrpc.common.loadbalance;

import conglin.clrpc.common.object.Pair;

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
     * @param type 类型
     * @param data 其中key为连接的ip地址，value为 服务器的元信息
     */
    void update(T type, Collection<K> data);

    /**
     * 根据type寻找第一个可用对象
     *
     * @param type 类型
     * @return
     */
    default V get(T type) {
        return get(type, 0);
    }

    /**
     * 根据指定算法返回对象
     *
     * @param type   类型
     * @param random
     * @return
     */
    default V get(T type, int random) {
        return getValue(type, random, v -> Boolean.TRUE);
    }

    /**
     * 根据条件返回第一个符合条件指定对象
     *
     * @param type      类型
     * @param predicate 条件
     * @return
     */
    default V getKey(T type, Predicate<K> predicate) {
        return getKey(type, 0, predicate);
    }

    /**
     * 根据type和random返回第一个符合条件的指定对象
     *
     * @param type      类型
     * @param random    随机数
     * @param predicate 条件
     * @return
     */
    V getKey(T type, int random, Predicate<K> predicate);

    /**
     * 根据条件返回第一个符合条件指定对象
     *
     * @param type      类型
     * @param predicate 条件
     * @return
     */
    default V getValue(T type, Predicate<V> predicate) {
        return getValue(type, 0, predicate);
    }


    /**
     * 根据type和random返回第一个符合条件的指定对象
     *
     * @param type      类型
     * @param random    随机数
     * @param predicate 条件
     * @return
     */
    V getValue(T type, int random, Predicate<V> predicate);

    /**
     * 根据type和random返回第一个符合条件的指定对象
     *
     * @param type      类型
     * @param random    随机数
     * @param predicate 条件
     * @return
     */
    Pair<K, V> getEntity(T type, int random, Predicate<K> predicate);

    /**
     * 获取所有类型
     *
     * @return
     */
    Collection<T> allTypes();

    /**
     * 是否存在该类型
     *
     * @param type 类型
     * @return
     */
    boolean hasType(T type);

    /**
     * 给定类型下是否有可用的对象
     *
     * @param type 类型
     * @return
     */
    default boolean hasNext(T type) {
        return hasType(type) && get(type) != null;
    }

    /**
     * 对所有节点做出某个操作
     *
     * @param consumer 处理
     */
    void forEach(Consumer<V> consumer);

    /**
     * 对所有节点做出某个操作
     *
     * @param <R>
     * @param function 处理
     * @return
     */
    <R> Collection<R> apply(Function<V, R> function);

    /**
     * 对某个类型的所有节点做出某个操作
     *
     * @param type     类型
     * @param consumer 处理
     */
    void forEach(T type, Consumer<V> consumer);

    /**
     * @param type     类型
     * @param function 处理
     * @param <R>
     * @return
     */
    <R> Collection<R> apply(T type, Function<V, R> function);

    /**
     * 清空容器内所有数据
     */
    void clear();

    /**
     * 是否为空
     * 
     * @return
     */
    boolean isEmpty();
}