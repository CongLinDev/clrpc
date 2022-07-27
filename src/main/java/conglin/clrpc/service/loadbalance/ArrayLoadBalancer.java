package conglin.clrpc.service.loadbalance;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于数组的负载均衡 默认为随机
 *
 * @param <K> key
 * @param <V> value
 */
public class ArrayLoadBalancer<K, V> extends AbstractLoadBalancer<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ArrayLoadBalancer.class);

    private static final Node<?, ?>[] EMPTY_NODES = new Node[0];
    private Node<K, V>[] nodes;
    private int size;

    public ArrayLoadBalancer() {
        @SuppressWarnings("unchecked")
        final Node<K, V>[] nodes = (Node<K, V>[]) EMPTY_NODES;
        this.nodes = nodes;
        this.size = 0;
    }

    @Override
    public void update(int epoch, Collection<K> data) {
        final int currentEpoch = getCurrentEpoch(epoch);
        if (currentEpoch < 0)
            return;

        final Node<K, V>[] nodes = this.nodes;
        final int size = this.size;

        @SuppressWarnings("unchecked")
        final Node<K, V>[] nextNodes = new Node[data.size()];
        int nextNodesIndex = 0;

        updateNextKey: for (K key : data) {
            for (int index = 0; index < size; index++) {
                final Node<K, V> node = nodes[index];
                if (node == null)
                    continue;
                if (!matcher.test(node.getKey(), key))
                    continue;
                // find match
                if (!node.setEpoch(currentEpoch)) {
                    LOGGER.warn("Node set epoch failed(currentEpoch={} epoch={} key={})", currentEpoch, node.getEpoch(),
                            key);
                    continue updateNextKey; // update next key
                }
                node.setKey(key);   // update key
                // migration
                nextNodes[nextNodesIndex++] = node;
                nodes[index] = null; // help next query
                LOGGER.info("Update old node(index={}, key={})", nextNodesIndex, key);
                continue updateNextKey; // update next key
            }
            // find nothing and create new node
            V v = this.convertor.apply(key);
            if (v != null) {
                LOGGER.info("Add new node(index={}, key={})", nextNodesIndex, key);
                nextNodes[nextNodesIndex++] = new Node<>(currentEpoch, key, v);
            } else {
                LOGGER.error("Null Object from {}", key);
            }
        }

        // 移除过期节点
        for (int i = 0; i < size; i++) {
            Node<K, V> node = nodes[i];
            if (node != null && node.getEpoch() < currentEpoch) {
                LOGGER.debug("Remove invalid node(key={})", node.getKey());
                destructor.accept(node.getValue());
            }
        }

        setNodes(nextNodes, nextNodesIndex);
    }

    @Override
    protected Node<K, V> getNode(int random, Predicate<Node<K, V>> predicate) {
        final Node<K, V>[] nodes = this.nodes;
        final int size = this.size;
        if (size == 0)
            return null;
        final int offset = offset(random, size);
        for (int index = offset; index < size; index++) {
            if (nodes[index] != null && predicate.test(nodes[index])) {
                record(index);
                return nodes[index];
            }
        }
        for (int index = 0; index < offset; index++) {
            if (nodes[index] != null && predicate.test(nodes[index])) {
                record(index);
                return nodes[index];
            }
        }
        return null;
    }

    @Override
    public void forEach(Consumer<V> consumer) {
        final Node<K, V>[] nodes = this.nodes;
        final int size = this.size;
        for (int i = 0; i < size; i++) {
            Node<K, V> node = nodes[i];
            if (node != null) {
                consumer.accept(node.getValue());
            }
        }
    }

    @Override
    public void clear() {
        @SuppressWarnings("unchecked")
        final Node<K, V>[] nodes = (Node<K, V>[]) EMPTY_NODES;
        this.nodes = nodes;
        this.size = 0;
    }

    @Override
    public boolean isEmpty() {
        return this.size == 0;
    }

    /**
     * 设置nodes
     * 
     * @param nodes
     * @param size
     */
    protected void setNodes(Node<K, V>[] nodes, int size) {
        this.nodes = nodes;
        this.size = size;
    }

    /**
     * 获取索引
     * 
     * @param random
     * @param size
     * @return
     */
    protected int offset(int random, int size) {
        return random & (size - 1);
    }

    /**
     * 记录当前 index
     * 
     * @param index
     */
    protected void record(int index) {

    }

}
