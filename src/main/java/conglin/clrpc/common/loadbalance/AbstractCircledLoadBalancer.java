package conglin.clrpc.common.loadbalance;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

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
abstract public class AbstractCircledLoadBalancer<T, K, V> implements LoadBalancer<T, K, V> {
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractCircledLoadBalancer.class);

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
    protected TreeMap<Integer, Node> circle;

    public AbstractCircledLoadBalancer() {
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
            Map.Entry<Integer, Node> entry = circle.higherEntry(next);
            V v = entry.getValue().getValue();
            consumer.accept(v);
            next = entry.getKey() + 1;
        }
    }

    @Override
    public boolean hasNext(T type) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return false;
        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号

        Integer next = circle.higherKey(head);
        if (next == null || next > tail)
            return false;
        return true;
    }

    @Override
    public void clear() {
        circle.clear();
        descriptions.clear();
    }

    @Override
    public V get(T type, Predicate<V> predicate) {
        // 遍历查找，寻找满足条件的 V
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if (regionAndEpoch == null)
            return null;
        // 获取当前区域范围 [head, tail]
        int head = regionHead(regionAndEpoch);
        int tail = regionTail(head);// 区域编号不得超过最大编号

        int next = head;
        while (next <= tail) {
            Map.Entry<Integer, Node> entry = circle.higherEntry(next);
            V v = entry.getValue().getValue();
            if (predicate.test(v))
                return v;
            next = entry.getKey() + 1;
        }

        return null;
    }

    @Override
    public void update(T type, Collection<Pair<K, String>> data) {
        AtomicInteger regionAndEpoch = null;
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
        LOGGER.debug("Update Region[head={}, tail={}], Epoch={}", head, tail, currentEpoch);

        // 遍历更新的数据，对给定的数据进行更新
        for (Pair<K, String> pair : data) {
            K key = pair.getFirst();
            String metaInfo = pair.getSecond();

            int position = hash(key) & _16_BIT_MASK | head;// 获取区域内部编号

            do {
                Node node = null;
                if ((node = circle.get(position)) == null) { // 插入新值
                    // 插入前再次检查 epoch
                    if ((currentEpoch == (regionAndEpoch.get() & _16_BIT_MASK))
                            && (node = createNode(key, metaInfo, currentEpoch)) != null) {
                        circle.put(position, node);
                        break;
                    }
                } else if (match(key, node.getValue()) && updateNode(node, metaInfo, currentEpoch)) { // 更新epoch
                    LOGGER.debug("Update old node = {}", key);
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
        Map.Entry<Integer, Node> entry;
        while ((entry = circle.higherEntry(position)) != null // 下一个节点不为空
                && (position = entry.getKey()) <= tail) { // 且下一个节点确保在范围内
            if (removeNode(entry.getValue(), currentEpoch)) { // 只移除上一代未更新的节点
                circle.remove(position);
            }
        }
    }

    /**
     * 返回区域起点
     * 
     * @param regionAndEpoch
     * @return
     */
    protected int regionHead(AtomicInteger regionAndEpoch) {
        return regionAndEpoch.get() & _32_16_BIT_MASK;
    }

    /**
     * 返回区域终点
     * 
     * @param regionHead
     * @return
     */
    protected int regionTail(int regionHead) {
        return regionHead | _16_BIT_MASK;
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
     * 创建节点
     * 
     * @param key
     * @param metaInfo
     * @param currentEpoch
     * @return
     */
    abstract protected Node createNode(K key, String metaInfo, int currentEpoch);

    /**
     * 更新节点
     * 
     * @param node
     * @param metaInfo
     * @param currentEpoch
     * @return
     */
    abstract protected boolean updateNode(Node node, String metaInfo, int currentEpoch);

    /**
     * 移除节点
     * 
     * @param node
     * @param currentEpoch
     * @return
     */
    abstract protected boolean removeNode(Node node, int currentEpoch);

    /**
     * 检查 K 与 V 是否匹配
     * 
     * @param key
     * @param value
     * @return
     */
    abstract protected boolean match(K key, V value);

    /**
     * 为 新的类型 在 {@link AbstractCircledLoadBalancer#circle} 申请一块可用的区域 确定区域并添加区域头节点
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
    protected Node createRegionHeadNode() {
        return new Node();
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

    class Node {

        protected AtomicInteger epoch;

        protected V value;

        // 元信息
        protected String metaInfo;

        public Node() {
            this(0, null);
        }

        public Node(V value) {
            this(0, value);
        }

        public Node(int epoch, V value) {
            this(epoch, value, "");
        }

        public Node(int epoch, V value, String metaInfo) {
            this.epoch = new AtomicInteger(epoch);
            this.value = value;
            this.metaInfo = metaInfo;
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
         * 获得Node的元信息
         * 
         * @return
         */
        public String getMetaInfo() {
            return metaInfo;
        }

        /**
         * 设置Node的元信息
         * 
         * @param metaInfo
         */
        public void setMetaInfo(String metaInfo) {
            this.metaInfo = metaInfo;
        }
    }
}