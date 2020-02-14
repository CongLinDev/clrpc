package conglin.clrpc.common.loadbalance;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import conglin.clrpc.common.Pair;

/**
 * 该类用作一致性哈希负载均衡 适合一个 type 对应多个 key-value 的负载均衡
 * 
 * @param <T> type
 * @param <K> key
 * @param <V> value
 */
public class ConsistentHashLoadBalancer<T, K, V> extends AbstractCircledLoadBalancer<T, K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    // 用于更新，将 K 转换为 V
    private final Function<K, V> convertor;
    // 用于销毁，将V销毁
    private final Consumer<V> destructor;

    public ConsistentHashLoadBalancer(Function<K, V> convertor) {
        this(convertor, v -> LOGGER.debug("Destroy object", v));
    }

    public ConsistentHashLoadBalancer(Function<K, V> convertor, Consumer<V> destructor) {
        super();
        this.convertor = convertor;
        this.destructor = destructor;
    }

    /**
     * 将 K 对象转换为 V 对象
     * 
     * @param key
     * @return
     */
    protected V convert(K key) {
        return convertor.apply(key);
    }

    /**
     * 将 V 销毁
     * 
     * @param value
     */
    protected void destroy(V value) {
        destructor.accept(value);
    }

    @Override
    public V get(T type, int random) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return null;

        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号
        int next = head | (random & _16_BIT_MASK);

        for (int count = 0; count < 2; count++) { // 检查两轮即可
            Map.Entry<Integer, Node<K, V>> entry = circle.higherEntry(next);

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

        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号
        int randomHash = hash(key);
        int next = head | (randomHash & _16_BIT_MASK);

        Node<K, V> node = null;
        while ((node = circle.get(next)) != null) {
            if (node.match(key))
                return node.getValue();

            if (++next > tail)
                next = head + 1;
        }
        return null;
    }

    @Override
    protected Node<K, V> createNode(Pair<K, String> data, int currentEpoch) {
        K key = data.getFirst();
        V v = convert(key);
        if (v != null) {
            LOGGER.debug("Add new node = {}", key);
            return new Node<K, V>(currentEpoch, v, data);
        } else {
            LOGGER.error("Null Object from {}", key);
            return null;
        }
    }

    @Override
    protected boolean updateNode(Node<K, V> node, Pair<K, String> data, int currentEpoch) {
        if (currentEpoch <= node.getEpoch())
            return false;
        if (node.setEpoch(currentEpoch)) {
            node.setMetaInfomation(data);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removeNode(Node<K, V> node, int currentEpoch) {
        if (node.getEpoch() + 1 == currentEpoch) {
            LOGGER.debug("Remove valid node.");
            destroy(node.getValue());
            return true;
        }
        return false;
    }
}