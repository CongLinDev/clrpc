package conglin.clrpc.common.loadbalance;

/**
 * 基于数组的负载均衡 轮询算法
 *
 * @param <K> key
 * @param <V> value
 */
public class RoundRobinLoadBalancer<K, V> extends ArrayLoadBalancer<K, V> {
    private int next;
    public RoundRobinLoadBalancer() {
        super();
    }

    @Override
    protected void record(int index) {
        this.next = index + 1;
    }

    @Override
    protected int offset(int random, int size) {
        final int next = this.next;
        if (next < size) return next;
        this.next = 0;
        return 0;
    }
}
