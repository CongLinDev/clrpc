package conglin.clrpc.service.loadbalance;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 该类用作一致性哈希负载均衡
 * 适合一个 type 对应多个 key-value 的负载均衡
 * @param <T> type
 * @param <K> key
 * @param <V> value
 */
public class ConsistentHashLoadBalancer<T, K, V> implements LoadBalancer<T, K, V> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsistentHashLoadBalancer.class);

    private final int _16_BIT_MASK = 0xFFFF;
    private final int _32_16_BIT_MASK = 0xFFFF0000;
    private final int _32_16_BIT_POSITIVE_MASK = 0x7FFF0000;

    // descriptions 中存储着 T 和 Integer
    // T为类型
    // Integer 高16位代表该键所在的区域 region 
    // 低16位代表 该键的 epoch 方便淘汰算法将无效的V值淘汰
    protected Map<T, AtomicInteger> descriptions;

    // circle 模拟环来存放 K-V
    // Integer 高16位为 descriptions 中的区域 region
    // 低16位为 hash(K) 将 K-V 打乱
    // 这样，只需要到指定的区域中寻找满足条件的值即可
    protected TreeMap<Integer, Node<V>> circle;

    // 用于自定义比较 K 和 V 是否匹配
    protected final BiPredicate<K, V> equalPredicate;

    public ConsistentHashLoadBalancer(BiPredicate<K, V> equalPredicate){
        this.equalPredicate = equalPredicate;
        descriptions = new ConcurrentHashMap<>();
        circle = new TreeMap<>();
    }

    @Override
    public void update(T type, Collection<K> data, Function<K, V> start, Consumer<V> stop) {
        AtomicInteger code = null;
        int epoch = 0;
        if((code = descriptions.get(type)) == null){
            code = firstUpdate(type);
            LOGGER.debug("First update region head number = " + code);
        }else{
            // 非第一次更新的话 更新epoch
            code.incrementAndGet();
            epoch = code.get() & _16_BIT_MASK;
            LOGGER.debug("Update region head number = " + (code.get() & _32_16_BIT_MASK) + " Epoch = " + epoch);
        }
        // 区域头节点的Hash值
        int head = code.get() & _32_16_BIT_MASK;
        // 创建或更新有效节点
        createOrUpdateNode(code, epoch, data, start); 
        // 删除无效节点 (不删除区域头节点)
        removeInvalidNode(head, epoch, stop);
    }

    @Override
    public V get(T type, int random) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if(regionAndEpoch == null) return null;
        int code = regionAndEpoch.get();

        int head = code & _32_16_BIT_MASK;
        int next = head | (random & _16_BIT_MASK);
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号
         
        for(int count = 0; count < 2; count++){ // 检查两轮即可
            Map.Entry<Integer, Node<V>> entry = circle.higherEntry(next);
            
            if(entry != null && (next = entry.getKey()) <= tail){
                return entry.getValue().getValue();
            }
            next = head + 1;
        }
        return null;
    }

    @Override
    public V get(T type, K key) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if(regionAndEpoch == null) return null;
        int code = regionAndEpoch.get();

        int randomHash = hash(key);
        int head = code & _32_16_BIT_MASK;
        int next = head | (randomHash & _16_BIT_MASK);
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        Node<V> node;
        while((node = circle.get(next)) != null){
            if(equalPredicate.test(key, node.getValue()))
                return node.getValue();

            if(++next > tail)
                next = head + 1;
        }
        return null;
    }

    @Override
    public V get(T type, Predicate<V> predicate) {
        // 遍历查找，寻找满足条件的 V
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if(regionAndEpoch == null) return null;
        int code = regionAndEpoch.get();

        int head = code & _32_16_BIT_MASK;
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        int next = head;
        while(next <= tail){
            Map.Entry<Integer, Node<V>> entry = circle.higherEntry(next);
            V v = entry.getValue().getValue();
            if(predicate.test(v)) return v;
            next = entry.getKey() + 1;
        }

        return null;
    }

    @Override
    public void forEach(Consumer<V> consumer) {
        if(consumer == null) return;
        circle.values().forEach( node ->
            consumer.accept(node.getValue())
        );
    }

    @Override
    public void forEach(T type, Consumer<V> consumer){
        if(consumer == null) return;

        AtomicInteger regionAndEpoch = descriptions.get(type);
        if(regionAndEpoch == null) return;
        int code = regionAndEpoch.get();

        int head = code & _32_16_BIT_MASK;
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        int next = head;
        while(next <= tail){
            Map.Entry<Integer, Node<V>> entry = circle.higherEntry(next);
            V v = entry.getValue().getValue();
            consumer.accept(v);
            next = entry.getKey() + 1;
        }
    }

    @Override
    public boolean hasNext(T type) {
        AtomicInteger regionAndEpoch = descriptions.get(type);
        if(regionAndEpoch == null) return false;
        int code = regionAndEpoch.get();

        int head = code & _32_16_BIT_MASK;
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        Integer next = circle.higherKey(head);
        if(next == null || next > tail)
            return false;
        return true;
    }

    @Override
    public void clear() {
        circle.clear();
        descriptions.clear();
    }

    /**
     * 首次更新
     * 确定区域并添加区域头节点
     * @param type
     * @return 返回 descriptions中 type 所对应的值。即前16位代表区域 region; 后16位代表 该键所在的区域 region。
     */
    private AtomicInteger firstUpdate(T type){
        int regionHead;
        int hashcode = type.hashCode();
        do{
            // 对hashcode再次hash计算
            hashcode = hash(hashcode);
            // 得到新区域的头节点 hashcode
            regionHead = hash(hashcode) & _32_16_BIT_POSITIVE_MASK;
        }while(circle.containsKey(regionHead));//直到不发生冲撞为止

        // 创建区域头节点
        // 头节点不保存任何值，只是一个区域的标志
        // 即代表这个区域已经被某个 Type 占用了
        circle.put(regionHead, new Node<V>());
        // 刚好 regionHead 的 低16位为0，即 epoch 为 0
        AtomicInteger regionAndEpoch = new AtomicInteger(regionHead);
        AtomicInteger temp = descriptions.putIfAbsent(type, regionAndEpoch); // 以最先放入的服务为准
        return temp == null ? regionAndEpoch : temp;
    }

    /**
     * 创建或更新有效节点
     * @param headAndEpoch 区域头节点和当前epoch
     * @param epoch 本轮的epoch
     * @param data 数据
     * @param start 添加V的工作
     */
    private void createOrUpdateNode(AtomicInteger headAndEpoch, int epoch, Collection<K> data, Function<K, V> start){
        int head = headAndEpoch.get() & _32_16_BIT_MASK;
        int tail = headAndEpoch.get() | _16_BIT_MASK;// 区域编号不得超过最大编号

        for(K key : data){
            int next = hash(key);//获取区域编号
            next = (next & _16_BIT_MASK) | head;

            do{
                Node<V> node;
                if((node = circle.get(next)) == null){ // 插入新值
                    if(start != null && epoch == (headAndEpoch.get() & _16_BIT_MASK)){
                        V v = start.apply(key);
                        if(v != null){
                            circle.put(next, new Node<V>(epoch, v));
                            LOGGER.debug("Add new node = " + key);
                        }
                    }
                    break;
                }else if(equalPredicate.test(key, node.getValue())){ // 更新epoch
                    if(epoch > node.getEpoch() && !node.setEpoch(epoch)) continue;
                    LOGGER.debug("Update old node = " + key);
                    break;
                }else{ // 发生冲撞
                    next++; // 将 v 更新到该节点的后面
                }
            }while(next <= tail);
        }
    }

    /**
     * 移除无效节点，即
     * 移除 {@link Node#getEpoch()} < currentEpoch 的节点
     * 范围为 (head, tail]
     * 其中 tail = head | _16_BIT_MASK
     * @param head 头节点位置
     * @param currentEpoch 当前 epoch
     * @param stop 结束工作
     */
    private void removeInvalidNode(int head, int currentEpoch, Consumer<V> stop){
        int tail = head | _16_BIT_MASK;// 尾节点位置

        int next = head;
        Map.Entry<Integer, Node<V>> entry;
        while((entry = circle.higherEntry(next)) != null // 下一个节点不为空
                &&  (next = entry.getKey()) <= tail){ // 且下一个节点确保在范围内
            if(entry.getValue().getEpoch() + 1 == currentEpoch){ // 只移除上一代未更新的节点
                circle.remove(entry.getKey());
                LOGGER.debug("Remove valid node.");
                if(stop != null){
                    stop.accept(entry.getValue().getValue());
                } 
                entry = null; // help gc
            }
        }
    }

    /**
     * hash函数
     * @param obj
     * @return
     */
    protected int hash(Object obj){
        return System.identityHashCode(obj);
    }
}

class Node<V>{
    private AtomicInteger epoch;
    private V value;

    public Node(){
        this(0, null);
    }

    public Node(V value){
        this(0, value);
    }

    public Node(int epoch, V value) {
        this.epoch = new AtomicInteger(epoch);
        this.value = value;
    }

    /**
     * 获得当前节点的代
     * @return
     */
    public int getEpoch(){
        return epoch.get();
    }

    /**
     * 设置当前节点的代
     * @param epoch
     * @return
     */
    public boolean setEpoch(int epoch){
        return this.epoch.compareAndSet(epoch - 1, epoch);
    }

    /**
     * 获取当前节点存储的值
     * @return
     */
    public V getValue(){
        return value;
    }
}