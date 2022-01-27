package conglin.clrpc.common.loadbalance;

import conglin.clrpc.common.object.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.*;
import java.util.stream.Collectors;


/**
 * 该类用作一致性哈希负载均衡 适合一个 type 对应多个 key-value 的负载均衡
 *
 * @param <T> type
 * @param <K> key
 * @param <V> value
 */
public class ConsistentHashLoadBalancer<T, K, V> implements LoadBalancer<T, K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    // mask
    protected final int _16_BIT_MASK = 0xFFFF;
    protected final int _32_16_BIT_MASK = 0xFFFF0000;
    protected final int _32_16_BIT_POSITIVE_MASK = 0x7FFF0000;

    // descriptions 中存储着 T 和 Integer
    // T为类型
    // Integer 即 <region, epoch> 由 region 和 epoch 组成
    // 高16位代表该键所在的区域 region
    // 低16位代表 该键的 epoch 方便淘汰算法将无效的V值淘汰
    protected Map<T, AtomicInteger> descriptions;

    // circle 模拟环来存放 K-V
    // Integer 高16位为 descriptions 中的区域 region
    // 低16位使用自定义方法 将 K-V 打乱或是顺序存储
    // 这样，只需要到指定的区域中寻找满足条件的值即可
    protected TreeMap<Integer, Node<K, V>> circle;

    // 用于更新，将 T 和 K 转换为 V
    private final BiFunction<T, K, V> convertor;
    // 用于销毁，将V销毁
    private final BiConsumer<T, V> destructor;

    public ConsistentHashLoadBalancer(BiFunction<T, K, V> convertor) {
        this(convertor, (type, value) -> LOGGER.debug("Destroy type={} object={}", type, value));
    }

    public ConsistentHashLoadBalancer(BiFunction<T, K, V> convertor, BiConsumer<T, V> destructor) {
        this.convertor = convertor;
        this.destructor = destructor;

        descriptions = new ConcurrentHashMap<>();
        circle = new TreeMap<>();
    }

    @Override
    public void forEach(Consumer<V> consumer) {
        if (consumer == null)
            return;
        circle.values().forEach(node -> {
            V v = node.getValue();
            if (v != null)
                consumer.accept(v);
        });
    }

    @Override
    public void forEach(T type, Consumer<V> consumer) {
        if (consumer == null)
            return;

        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return;
        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号

        int next = head;
        while (next <= tail) {
            Map.Entry<Integer, Node<K, V>> entry = circle.higherEntry(next);
            if (entry == null)
                break;
            V v = entry.getValue().getValue();
            if (v != null)
                consumer.accept(v);
            next = entry.getKey() + 1;
        }
    }

    @Override
    public <R> Collection<R> apply(Function<V, R> function) {
        if (function == null)
            return Collections.emptyList();
        return circle.values().stream().map(Node::getValue).filter(Objects::nonNull).map(function)
                .collect(Collectors.toList());
    }

    @Override
    public <R> Collection<R> apply(T type, Function<V, R> function) {
        if (function == null)
            return Collections.emptyList();

        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return Collections.emptyList();
        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号

        int next = head;
        List<R> resultList = new ArrayList<>();
        while (next <= tail) {
            Map.Entry<Integer, Node<K, V>> entry = circle.higherEntry(next);
            if (entry == null)
                break;
            V v = entry.getValue().getValue();
            if (v != null)
                resultList.add(function.apply(v));
            next = entry.getKey() + 1;
        }
        return resultList;
    }

    @Override
    public Collection<T> allTypes() {
        return descriptions.keySet();
    }

    @Override
    public boolean hasType(T type) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        return regionAndEpoch != null;
    }

    @Override
    public void clear() {
        circle.clear();
        descriptions.clear();
    }

    @Override
    public boolean isEmpty() {
        return circle.isEmpty() && descriptions.isEmpty();
    }

    @Override
    public V getKey(T type, int random, Predicate<K> predicate) {
        Node<K, V> n = getNode(type, random, node -> predicate.test(node.getKey()));
        if (n == null) return null;
        return n.getValue();
    }

    @Override
    public V getValue(T type, int random, Predicate<V> predicate) {
        Node<K, V> n = getNode(type, random, node -> predicate.test(node.getValue()));
        if (n == null) return null;
        return n.getValue();
    }

    @Override
    public Pair<K, V> getEntity(T type, int random, Predicate<K> predicate) {
        Node<K, V> n = getNode(type, random, node -> predicate.test(node.getKey()));
        if (n == null) return null;
        return new Pair<>(n.getKey(), n.getValue());
    }

    /**
     * 获取满足条件的节点
     *
     * @param type
     * @param predicate
     * @return
     */
    protected Node<K, V> getNode(T type, int random, Predicate<Node<K, V>> predicate) {
        // 遍历查找，寻找满足条件的 V
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return null;
        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号

        int offset = regionOffset(head, random);
        int next = offset;
        // 遍历 [offset, tail]
        while (next <= tail) {
            Map.Entry<Integer, Node<K, V>> entry = circle.higherEntry(next);
            if (entry == null) {
                break;
            }
            if (predicate.test(entry.getValue()))
                return entry.getValue();
            next = entry.getKey() + 1;
        }
        // 遍历 [head, offset)
        next = head;
        while (next < offset) {
            Map.Entry<Integer, Node<K, V>> entry = circle.higherEntry(next);
            if (entry == null) {
                break;
            }
            if (predicate.test(entry.getValue()))
                return entry.getValue();
            next = entry.getKey() + 1;
        }
        return null;
    }

    @Override
    public void update(T type, Collection<K> data) {
        AtomicInteger regionAndEpoch;
        int currentEpoch = 0;

        if ((regionAndEpoch = descriptions.get(type)) == null) {
            // 首次更新需要申请一个可用的区域
            regionAndEpoch = applyForAvailableRegion(type);
            LOGGER.debug("Apply for new Region.");
        } else {
            // 非首次更新的话 更新epoch
            currentEpoch = regionAndEpoch.incrementAndGet() & _16_BIT_MASK;
        }
        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号
        LOGGER.info("Update Region[head={}, tail={}], Epoch={}", head, tail, currentEpoch);

        // 遍历更新的数据，对给定的数据进行更新
        for (K key : data) {
            int position = hash(key) & _16_BIT_MASK | head;// 获取区域内部编号

            do {
                Node<K, V> node;
                if ((node = circle.get(position)) == null) { // 插入新值
                    // 插入前再次检查 epoch
                    if (currentEpoch == (regionAndEpoch.get() & _16_BIT_MASK)) {
                        V v = convertor.apply(type, key);
                        if (v != null) {
                            LOGGER.info("Add new node(position={}, key={})", position, key);
                            circle.put(position, new Node<>(currentEpoch, key, v));
                        } else {
                            LOGGER.error("Null Object from {}", key);
                        }
                        break;
                    }
                } else if (node.match(key) && node.setEpoch(currentEpoch)) { // 更新epoch
                    node.setKey(key);
                    LOGGER.info("Update old node(position={}, key={})", position, key);
                    if (node.getValue() == null) {
                        node.setValue(convertor.apply(type, key));
                    }
                    break;
                } else { // 发生冲撞
                    LOGGER.warn("Hash collision. Consider to replace a hash algorithm for load balancer.");
                    if (++position >= tail) { // 将 v 更新到该节点的后面
                        position = head | 1;
                    }
                }
            } while (true);
        }

        // 移除过期节点
        int position = head;
        Map.Entry<Integer, Node<K, V>> entry;
        while ((entry = circle.higherEntry(position)) != null // 下一个节点不为空
                && (position = entry.getKey()) <= tail) { // 且下一个节点确保在范围内
            Node<K, V> node = entry.getValue();
            if (node.getEpoch() + 1 == currentEpoch) { // 只移除上一代未更新的节点
                circle.remove(position);
                LOGGER.debug("Remove valid node(position={}, key={})", position, node.getKey());
                destructor.accept(type, node.getValue());
            }
        }
    }

    /**
     * 返回区域起点
     *
     * @param regionAndEpoch 区域
     * @return
     */
    protected int regionHead(AtomicInteger regionAndEpoch) {
        return regionAndEpoch.get() & _32_16_BIT_MASK;
    }

    /**
     * 返回区域终点
     *
     * @param regionHead 区域起点
     * @return
     */
    protected int regionTail(int regionHead) {
        return regionHead | _16_BIT_MASK;
    }

    /**
     * 返回区域内部的一个点
     *
     * @param regionHead 区域起点
     * @param offset     偏移量
     * @return
     */
    protected int regionOffset(int regionHead, int offset) {
        return (offset & _16_BIT_MASK) | regionHead;
    }

    /**
     * 返回区域终点
     *
     * @param regionAndEpoch
     * @return
     */
    protected int regionTail(AtomicInteger regionAndEpoch) {
        return regionTail(regionHead(regionAndEpoch));
    }

    /**
     * 为 新的类型 在 {@link ConsistentHashLoadBalancer#circle} 申请一块可用的区域 确定区域并添加区域头节点
     * 该方法应该与首次更新时调用
     *
     * @param type
     * @return 返回 descriptions中 type 所对应的值。即前16位代表区域 region; 后16位代表 该键所在的区域 region。
     */
    protected AtomicInteger applyForAvailableRegion(T type) {
        int regionHead;
        int hashcode = type.hashCode();
        do {
            // 对hashcode再次hash计算
            hashcode = hash(hashcode);
            // 得到新区域的头节点 hashcode
            regionHead = hash(hashcode) & _32_16_BIT_POSITIVE_MASK;
        } while (circle.containsKey(regionHead));// 直到不发生冲撞为止

        // 创建区域头节点
        // 头节点不保存任何值，只是一个区域的标志
        // 即代表这个区域已经被某个 Type 占用了
        circle.put(regionHead, createRegionHeadNode());
        // 刚好 regionHead 的 低16位为0，即 epoch 为 0
        AtomicInteger regionAndEpoch = new AtomicInteger(regionHead);
        AtomicInteger temp = descriptions.putIfAbsent(type, regionAndEpoch); // 以最先放入的服务为准
        return temp == null ? regionAndEpoch : temp;
    }

    /**
     * 创建一个区域头节点
     *
     * @return
     */
    protected Node<K, V> createRegionHeadNode() {
        return new Node<>();
    }

    /**
     * hash函数
     *
     * @param obj
     * @return
     */
    protected int hash(Object obj) {
        return System.identityHashCode(obj);
    }

    static class Node<K, V> {

        protected AtomicInteger epoch;

        protected K key;

        protected V value;

        public Node() {
            this(0, null, null);
        }

        public Node(int epoch, K key, V value) {
            this.epoch = new AtomicInteger(epoch);
            this.key = key;
            this.value = value;
        }

        /**
         * 获得当前节点的代
         *
         * @return
         */
        public int getEpoch() {
            return epoch.get();
        }

        /**
         * 设置当前节点的代
         *
         * @param epoch
         * @return
         */
        public boolean setEpoch(int epoch) {
            return this.epoch.compareAndSet(epoch - 1, epoch);
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

        public K getKey() {
            return key;
        }

        public void setKey(K key) {
            this.key = key;
        }

        /**
         * 检查是否匹配
         *
         * @param key
         * @return
         */
        public boolean match(K key) {
            if (this.key == null) return false; // empty node
            return this.key.equals(key);
        }
    }
}