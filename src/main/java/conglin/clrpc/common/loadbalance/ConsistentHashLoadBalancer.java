package conglin.clrpc.common.loadbalance;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.object.Pair;

/**
 * 该类用作一致性哈希负载均衡
 *
 * @param <K> key
 * @param <V> value
 */
public class ConsistentHashLoadBalancer<K, V> implements LoadBalancer<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    // circle 模拟环来存放 K-V
    protected final TreeMap<Integer, Node<K, V>> circle;

    // 用于构造，将 K 转换为 V
    private Function<K, V> convertor;
    // 用于销毁，将V销毁
    private Consumer<V> destructor;
    // 用于比较
    private BiPredicate<K, K> matcher;

    private volatile int mainEpoch;

    public ConsistentHashLoadBalancer() {
        circle = new TreeMap<>();
    }

    @Override
    public void update(Collection<K> data) {
        update(-1, data);
    }

    public void update(final int epoch, Collection<K> data) {
        int currentEpoch = epoch;
        if (epoch < 0) {
            currentEpoch = ++mainEpoch;
        } else if (epoch <= mainEpoch) {
            LOGGER.warn("Epoch={} is smaller than mainEpoch={} and data will be ignored.", epoch, mainEpoch);
            return;
        } else {
            mainEpoch = epoch;
        }
        LOGGER.info("LoadBalancer will update Data Epoch={}", currentEpoch);

        // 遍历更新的数据，对给定的数据进行更新
        for (K key : data) {
            int position = position(key);// 获取区域内部编号

            while (true) {
                Node<K, V> node;
                if ((node = circle.get(position)) == null) { // 新增
                    V v = this.convertor.apply(key);
                    if (v != null) {
                        LOGGER.info("Add new node(position={}, key={})", position, key);
                        circle.put(position, new Node<>(currentEpoch, key, v));
                    } else {
                        LOGGER.error("Null Object from {}", key);
                    }
                    break;
                } else if (matcher.test(node.getKey(), key)) { // 更新
                    if (!node.setEpoch(currentEpoch)) {
                        break;
                    }
                    if (!key.equals(node.getKey())) {
                        V v = this.convertor.apply(key);
                        if (v != null) {
                            LOGGER.info("Add new node(position={}, key={})", position, key);
                            this.destructor.accept(node.getValue());
                            node.setKey(key);
                            node.setValue(v);
                        } else {
                            LOGGER.error("Null Object from {}", key);
                        }
                    }
                    LOGGER.info("Update old node(position={}, key={})", position, key);
                    break;
                } else { // 发生冲撞
                    LOGGER.warn("Hash collision. Consider to replace a hash algorithm for load balancer.");
                    position++;
                }
            }
        }

        // 移除过期节点
        Iterator<Node<K, V>> iterator = circle.values().iterator();
        while (iterator.hasNext()) {
            Node<K, V> node = iterator.next();
            if (node.getEpoch() < currentEpoch) { // 只移除未更新的节点
                iterator.remove();
                LOGGER.debug("Remove invalid node(key={})", node.getKey());
                destructor.accept(node.getValue());
            }
        }
    }

    @Override
    public V getKey(int random, Predicate<K> predicate) {
        Node<K, V> n = getNode(random, node -> predicate.test(node.getKey()));
        if (n == null)
            return null;
        return n.getValue();
    }

    @Override
    public V getValue(int random, Predicate<V> predicate) {
        Node<K, V> n = getNode(random, node -> predicate.test(node.getValue()));
        if (n == null)
            return null;
        return n.getValue();
    }

    @Override
    public Pair<K, V> getEntity(int random, Predicate<K> keyPredicate, Predicate<V> valuePredicate) {
        Node<K, V> n = getNode(random,
                node -> keyPredicate.test(node.getKey()) && valuePredicate.test(node.getValue()));
        if (n == null)
            return null;
        return new Pair<>(n.getKey(), n.getValue());
    }

    /**
     * 获取满足条件的节点
     *
     * @param type
     * @param predicate
     * @return
     */
    protected Node<K, V> getNode(int random, Predicate<Node<K, V>> predicate) {
        final int offset = offset(random);
        int next = offset;
        Map.Entry<Integer, Node<K, V>> entry;

        // 遍历 [offset, tail]
        while ((entry = circle.higherEntry(next)) != null) {
            if (predicate.test(entry.getValue()))
                return entry.getValue();
            next = entry.getKey() + 1;
        }

        // 遍历 [head, offset)
        next = circle.firstKey();
        while ((entry = circle.higherEntry(next)) != null && entry.getKey() < offset) {
            if (predicate.test(entry.getValue()))
                return entry.getValue();
            next = entry.getKey() + 1;
        }

        return null;
    }

    @Override
    public void forEach(Consumer<V> consumer) {
        circle.values().forEach(node -> consumer.accept(node.getValue()));
    }

    @Override
    public void clear() {
        circle.clear();
    }

    @Override
    public boolean isEmpty() {
        return circle.isEmpty();
    }

    @Override
    public void setConvertor(Function<K, V> convertor) {
        this.convertor = convertor;
    }

    @Override
    public void setDestructor(Consumer<V> destructor) {
        this.destructor = destructor;
    }

    @Override
    public void setMatcher(BiPredicate<K, K> matcher) {
        this.matcher = matcher;
    }

    /**
     * position
     * 
     * @param obj
     * @return
     */
    protected int position(Object obj) {
        return obj.hashCode();
    }

    /**
     * offset
     * 
     * @param random
     * @return
     */
    protected int offset(int random) {
        return random;
    }

    static class Node<K, V> {

        protected volatile int epoch;

        protected K key;

        protected V value;

        public Node() {
            this(0, null, null);
        }

        public Node(int epoch, K key, V value) {
            this.epoch = epoch;
            this.key = key;
            this.value = value;
        }

        /**
         * 获得当前节点的代
         *
         * @return
         */
        public int getEpoch() {
            return epoch;
        }

        /**
         * 设置当前节点的代
         *
         * @param epoch
         * @return
         */
        public boolean setEpoch(int epoch) {
            if (this.epoch < epoch) {
                this.epoch = epoch;
                return true;
            } else {
                return false;
            }
        }

        /**
         * 获取当前节点存储的值
         *
         * @return
         */
        public V getValue() {
            return value;
        }

        /**
         * 设置存储的值
         *
         * @param value
         */
        public void setValue(V value) {
            this.value = value;
        }

        /**
         * 获取 key
         * 
         * @return
         */
        public K getKey() {
            return key;
        }

        /**
         * 设置 key
         * 
         * @param key
         */
        public void setKey(K key) {
            this.key = key;
        }
    }
}
