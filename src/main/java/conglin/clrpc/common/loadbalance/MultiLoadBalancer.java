package conglin.clrpc.common.loadbalance;

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

import conglin.clrpc.common.object.Pair;

public interface MultiLoadBalancer<T, K, V> {

    /**
     * 添加 {@link LoadBalancer}
     * 
     * @param type
     * @param loadBalancer
     * @return
     */
    boolean addLoadBalancer(T type, LoadBalancer<K, V> loadBalancer);

    /**
     * 获取 {@link LoadBalancer0}
     * 
     * @param type
     * @return
     */
    LoadBalancer<K, V> getLoadBalancer(T type);

    /**
     * 使用集合更新数据
     *
     * @param type 类型
     * @param data 其中key为连接的ip地址，value为 服务器的元信息
     */
    default void update(T type, Collection<K> data) {
        LoadBalancer<K, V> loadBalancer = getLoadBalancer(type);
        if (loadBalancer == null)
            return;
        loadBalancer.update(data);
    }

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
    default V getKey(T type, int random, Predicate<K> predicate) {
        LoadBalancer<K, V> loadBalancer = getLoadBalancer(type);
        if (loadBalancer == null)
            return null;
        return loadBalancer.getKey(random, predicate);
    }

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
    default V getValue(T type, int random, Predicate<V> predicate) {
        LoadBalancer<K, V> loadBalancer = getLoadBalancer(type);
        if (loadBalancer == null)
            return null;
        return loadBalancer.getValue(random, predicate);
    }

    /**
     * 根据type和random返回第一个符合条件的指定对象
     *
     * @param type           类型
     * @param random         随机数
     * @param keyPredicate   条件
     * @param valuePredicate 条件
     * @return
     */
    default Pair<K, V> getEntity(T type, int random, Predicate<K> keyPredicate, Predicate<V> valuePredicate) {
        LoadBalancer<K, V> loadBalancer = getLoadBalancer(type);
        if (loadBalancer == null)
            return null;
        return loadBalancer.getEntity(random, keyPredicate, valuePredicate);
    }

    /**
     * 获取所有类型
     *
     * @return
     */
    Set<T> allTypes();

    /**
     * 是否存在某种类型
     * 
     * @param type
     * @return
     */
    boolean hasType(T type);

    /**
     * 对所有节点做出某个操作
     *
     * @param consumer 处理
     */
    void forEach(Consumer<V> consumer);

    /**
     * 清空容器内所有数据
     */
    void clearLoadBalancer();

    /**
     * 是否为空
     * 
     * @return
     */
    boolean isEmpty();
}
