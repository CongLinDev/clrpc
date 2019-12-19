package conglin.clrpc.service.loadbalance;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 该类用作一致性哈希负载均衡 适合一个 type 对应多个 key-value 的负载均衡
 * 
 * @param <T> type
 * @param <K> key
 * @param <V> value
 */
public class ConsistentHashLoadBalancer<T, K, V> extends AbstractCircledLoadBalancer<T, K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    // 用于自定义比较 K 和 V 是否匹配
    protected final BiPredicate<K, V> equalPredicate;

    public ConsistentHashLoadBalancer(BiPredicate<K, V> equalPredicate) {
        super();
        this.equalPredicate = equalPredicate;
    }

    @Override
    public V get(T type, int random) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return null;
        int code = regionAndEpoch.get();

        int head = code & _32_16_BIT_MASK;
        int next = head | (random & _16_BIT_MASK);
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        for (int count = 0; count < 2; count++) { // 检查两轮即可
            Map.Entry<Integer, Node<V>> entry = circle.higherEntry(next);

            if (entry != null && (next = entry.getKey()) <= tail) {
                return entry.getValue().getValue();
            }
            next = head + 1;
        }
        return null;
    }

    @Override
    public V get(T type, K key) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return null;
        int code = regionAndEpoch.get();

        int randomHash = hash(key);
        int head = code & _32_16_BIT_MASK;
        int next = head | (randomHash & _16_BIT_MASK);
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        Node<V> node = null;
        while ((node = circle.get(next)) != null) {
            if (equalPredicate.test(key, node.getValue()))
                return node.getValue();

            if (++next > tail)
                next = head + 1;
        }
        return null;
    }

    @Override
    protected void createOrUpdateNode(AtomicInteger headAndEpoch, int currentEpoch, Map<K, String> data,
            Function<K, V> start) {
        int head = headAndEpoch.get() & _32_16_BIT_MASK;
        int tail = headAndEpoch.get() | _16_BIT_MASK;// 区域编号不得超过最大编号

        for (Map.Entry<K, String> entry : data.entrySet()) {
            K key = entry.getKey();
            String metaInfo = entry.getValue();

            int next = hash(key);// 获取区域编号
            next = (next & _16_BIT_MASK) | head;

            do {
                Node<V> node = null;
                if ((node = circle.get(next)) == null) { // 插入新值
                    if (start != null && currentEpoch == (headAndEpoch.get() & _16_BIT_MASK)) {
                        V v = start.apply(key);
                        if (v != null) {
                            circle.put(next, new Node<V>(currentEpoch, v, metaInfo));
                            LOGGER.debug("Add new node = " + key);
                        }
                    }
                    break;
                } else if (equalPredicate.test(key, node.getValue())) { // 更新epoch
                    if (currentEpoch > node.getEpoch() && !node.setEpoch(currentEpoch))
                        continue;
                    node.setMetaInfo(metaInfo);
                    LOGGER.debug("Update old node = " + key);
                    break;
                } else { // 发生冲撞
                    next++; // 将 v 更新到该节点的后面
                }
            } while (next <= tail);
        }
    }

    @Override
    protected void removeInvalidNode(AtomicInteger headAndEpoch, int currentEpoch, Consumer<V> stop) {
        int head = headAndEpoch.get() & _32_16_BIT_MASK;
        int tail = head | _16_BIT_MASK;// 尾节点位置

        int next = head;
        Map.Entry<Integer, Node<V>> entry;
        while ((entry = circle.higherEntry(next)) != null // 下一个节点不为空
                && (next = entry.getKey()) <= tail) { // 且下一个节点确保在范围内
            if (entry.getValue().getEpoch() + 1 == currentEpoch) { // 只移除上一代未更新的节点
                circle.remove(entry.getKey());
                LOGGER.debug("Remove valid node.");
                if (stop != null) {
                    stop.accept(entry.getValue().getValue());
                }
                entry = null; // help gc
            }
        }
    }

}