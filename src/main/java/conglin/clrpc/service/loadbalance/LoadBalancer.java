package conglin.clrpc.service.loadbalance;

import java.util.Collection;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import conglin.clrpc.common.object.Pair;

/**
 * 该接口用作负载均衡 适合 {一个 type 对应多个 key-value 对} 组的负载均衡
 *
 * @param <T> type
 * @param <K> key
 * @param <V> value
 */
public interface LoadBalancer<K, V> {

    /**
     * 使用集合更新数据
     *
     * @param data 其中key为连接的ip地址，value为 服务器的元信息
     */
    default void update(Collection<K> data) {
        update(-1, data);
    }

    /**
     * 使用集合更新数据
     * 
     * @param epoch 为负数时表示最新的数据
     * @param data
     */
    void update(int epoch, Collection<K> data);

    /**
     * 寻找第一个可用对象
     *
     * @return
     */
    default V get() {
        return get(0);
    }

    /**
     * 根据指定算法返回对象
     *
     * @param random
     * @return
     */
    default V get(int random) {
        return getValue(random, v -> Boolean.TRUE);
    }

    /**
     * 根据条件返回第一个符合条件指定对象
     *
     * @param predicate 条件
     * @return
     */
    default V getKey(Predicate<K> predicate) {
        return getKey(0, predicate);
    }

    /**
     * 根据type和random返回第一个符合条件的指定对象
     *
     * @param random    随机数
     * @param predicate 条件
     * @return
     */
    V getKey(int random, Predicate<K> predicate);

    /**
     * 根据条件返回第一个符合条件指定对象
     *
     * @param predicate 条件
     * @return
     */
    default V getValue(Predicate<V> predicate) {
        return getValue(0, predicate);
    }

    /**
     * 根据type和random返回第一个符合条件的指定对象
     *
     * @param random    随机数
     * @param predicate 条件
     * @return
     */
    V getValue(int random, Predicate<V> predicate);

    /**
     * 根据random返回第一个符合条件的指定对象
     *
     * @param random         随机数
     * @param keyPredicate   条件
     * @param valuePredicate 条件
     * @return
     */
    Pair<K, V> getEntity(int random, Predicate<K> keyPredicate, Predicate<V> valuePredicate);

    /**
     * 对所有节点做出某个操作
     *
     * @param consumer 处理
     */
    void forEach(Consumer<V> consumer);

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

    /**
     * 设置转换器 由 K 得到 V
     * 
     * @param convertor
     */
    void setConvertor(Function<K, V> convertor);

    /**
     * 设置销毁器 销毁 V
     * 
     * @param destructor
     */
    void setDestructor(Consumer<V> destructor);

    /**
     * 设置匹配器
     * 
     * @param matcher
     */
    void setMatcher(BiPredicate<K, K> matcher);
}
