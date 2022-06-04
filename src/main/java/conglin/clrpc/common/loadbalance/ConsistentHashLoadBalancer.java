package conglin.clrpc.common.loadbalance;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 一致性哈希负载均衡
 *
 * @param <K> key
 * @param <V> value
 */
public class ConsistentHashLoadBalancer<K, V> extends AbstractLoadBalancer<K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    // circle 模拟环来存放 K-V
    protected final TreeMap<Integer, Node<K, V>> circle;

    public ConsistentHashLoadBalancer() {
        circle = new TreeMap<>();
    }

    @Override
    public void update(final int epoch, Collection<K> data) {
        final int currentEpoch = getCurrentEpoch(epoch);
        if (currentEpoch < 0) return;

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
                        LOGGER.warn("Node set epoch failed(currentEpoch={} epoch={} key={})", currentEpoch, node.getEpoch(), key);
                        break;
                    }
                    if (!key.equals(node.getKey())) {
                        V v = this.convertor.apply(key);
                        if (v != null) {
                            this.destructor.accept(node.getValue());
                            node.setKey(key);
                            node.setValue(v);
                            LOGGER.info("Add new node(position={}, key={})", position, key);
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
        try {
            next = circle.firstKey();
        } catch (NoSuchElementException e) {
            return null;
        }
        
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
}
