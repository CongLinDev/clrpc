package conglin.clrpc.service.loadbalance;

import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.object.Pair;

abstract public class AbstractLoadBalancer<K, V> implements LoadBalancer<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractLoadBalancer.class);

    // 用于构造，将 K 转换为 V
    protected Function<K, V> convertor;
    // 用于销毁，将V销毁
    protected Consumer<V> destructor;
    // 用于比较
    protected BiPredicate<K, K> matcher;

    private volatile int mainEpoch;

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
     * 获取符合条件的节点
     * 
     * @param random
     * @param predicate
     * @return
     */
    abstract protected Node<K, V> getNode(int random, Predicate<Node<K, V>> predicate);

    /**
     * 获取当前轮次的 epoch
     * 
     * @param epoch
     * @return 若返回负数，不进行更新
     */
    final protected int getCurrentEpoch(int epoch) {
        int currentEpoch = epoch;
        if (epoch < 0) {
            currentEpoch = ++mainEpoch;
        } else if (epoch <= mainEpoch) {
            LOGGER.warn("Epoch={} is smaller than mainEpoch={} and data will be ignored.", epoch, mainEpoch);
            return -1;
        } else {
            mainEpoch = epoch;
        }
        LOGGER.info("LoadBalancer will update Data Epoch={}", currentEpoch);
        return currentEpoch;
    }

    /**
     * 节点
     */
    protected static class Node<K, V> {

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
