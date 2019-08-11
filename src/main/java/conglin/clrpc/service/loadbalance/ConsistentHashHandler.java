package conglin.clrpc.service.loadbalance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 该类用作一致性哈希负载均衡
 * 适合一个 key 对应多个 value 的负载均衡
 * @param <K> key的类型
 * @param <V> value的类型
 */
public class ConsistentHashHandler<K, V extends Comparable<String>> implements LoadBalanceHandler<K, V> {

    private static final Logger log = LoggerFactory.getLogger(ConsistentHashHandler.class);

    private final int _16_BIT_MASK = 0xFFFF;
    private final int _32_16_BIT_MASK = 0xFFFF0000;
    private final int _32_16_BIT_POSITIVE_MASK = 0x7FFF0000;

    // descriptions 中存储着 K 和 Integer
    // K为一致性哈希的键
    // Integer 高16位代表该键所在的区域 region 
    // 低16位代表 该键的 epoch 方便淘汰算法将无效的V值淘汰
    private Map<K, Integer> descriptions;

    // circle 模拟环来存放 V
    // Integer 高16位为 descriptions 中的区域 region
    // 低16位为 hash(V) 将 V 打乱
    // 这样，只需要到指定的区域中寻找满足条件的值即可
    private TreeMap<Integer, Node<V>> circle;

    public ConsistentHashHandler(){
        descriptions = new HashMap<>();
        circle = new TreeMap<>();
    }

    @Override
    public void update(K key, List<String> data, Function<String, V> start, Consumer<V> stop) {
        Integer code = null;
        int epoch = 0;
        if((code = descriptions.get(key)) == null){
            code = firstUpdate(key);
            log.debug("First update region head number = " + code);
        }else{
            // 非第一次更新的话 更新epoch
            epoch = code & _16_BIT_MASK;
            epoch++; 
            descriptions.put(key, (code & _32_16_BIT_MASK) | epoch);
            log.debug("Update region head number = " + (code & _32_16_BIT_MASK) + " Epoch = " + epoch);
        }
        // 区域头节点的Hash值
        int head = code & _32_16_BIT_MASK;
        // 创建或更新有效节点
        createOrUpdateNode(head, epoch, data, start); 
        // 删除无效节点 (不删除区域头节点)
        removeInvalidNode(head, epoch, stop);
    }

    @Override
    public V get(K key, Object random) {
        Integer code;
        if((code = descriptions.get(key)) == null) return null;

        int randomHash = hash(random);
        int head = code & _32_16_BIT_MASK;
        int next = head | (randomHash & _16_BIT_MASK);
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
    public V get(K key, String string) {
        Integer code;
        if((code = descriptions.get(key)) == null) return null;

        int randomHash = hash(string);
        int head = code & _32_16_BIT_MASK;
        int next = head | (randomHash & _16_BIT_MASK);
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        Node<V> node;
        while((node = circle.get(next)) != null){
            if(node.getValue().compareTo(string) == 0)
                return node.getValue();

            if(++next > tail)
                next = head + 1;
        }
        return null;
    }

    @Override
    public V get(K key, Predicate<V> predicate) {
        // 遍历查找，寻找满足条件的 V
        Integer code;
        if((code = descriptions.get(key)) == null) return null;
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
        circle.values().forEach( node -> {
            V v = node.getValue();
            if(v != null)
                consumer.accept(v);
        });
    }

    @Override
    public boolean hasNext(K key) {
        Integer code;
        if((code = descriptions.get(key)) == null) return false;
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
     * @param key
     * @return 返回 descriptions中 key所对应的值。即前16位代表区域 region; 后16位代表 该键所在的区域 region。
     */
    private int firstUpdate(K key){
        int regionHead;
        int hashcode = key.hashCode();
        do{
            // 对hashcode再次hash计算
            hashcode = hash(hashcode);
            // 得到新区域的头节点 hashcode
            regionHead = hash(hashcode) & _32_16_BIT_POSITIVE_MASK;
        }while(circle.containsKey(regionHead));//直到不发生冲撞为止

        // 创建区域头节点
        // 头节点不保存任何值，只是一个区域的标志
        // 即代表这个区域已经被某个 Key 占用了
        circle.put(regionHead, new Node<V>());
        // 刚好 regionHead 的 低16位为0，即 epoch 为 0
        descriptions.put(key, regionHead);
        return regionHead;
    }

    /**
     * 创建或更新有效节点
     * @param head 区域头节点
     * @param epoch
     * @param data 数据
     * @param start 添加V的工作
     */
    private void createOrUpdateNode(int head, int epoch, List<String> data, Function<String, V> start){
        int tail = head | _16_BIT_MASK;// 区域编号不得超过最大编号

        for(String s : data){
            int next = hash(s);//获取区域编号
            next = (next & _16_BIT_MASK) | head;

            do{
                Node<V> node;
                if((node = circle.get(next)) == null){ // 插入新值
                    if(start != null){
                        circle.put(next, new Node<V>(epoch, start.apply(s)));
                        log.debug("Add new node = " + s);
                    }
                    break;
                }else if(node.getValue().compareTo(s) == 0){ // 更新epoch
                    node.setEpoch(epoch);
                    log.debug("Update old node = " + s);
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
            if(entry.getValue().getEpoch() < currentEpoch){
                circle.remove(entry.getKey());
                log.debug("Remove valid node.");
                if(stop != null){
                    stop.accept(entry.getValue().getValue());
                } 
                entry = null; // help gc
            }
        }
    }

    private int hash(Object obj){
        return System.identityHashCode(obj);
    }

}

class Node<V>{
    private int epoch;
    private V value;

    public Node(){
        this(0, null);
    }

    public Node(V value){
        this(0, value);
    }

    public Node(int epoch, V value) {
        this.epoch = epoch;
        this.value = value;
    }

    public int getEpoch(){
        return epoch;
    }

    public void setEpoch(int epoch){
        this.epoch = epoch;
    }

    public V getValue(){
        return value;
    }
}