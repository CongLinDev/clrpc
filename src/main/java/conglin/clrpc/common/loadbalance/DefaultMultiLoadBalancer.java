package conglin.clrpc.common.loadbalance;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.function.Function;

public class DefaultMultiLoadBalancer<T, K, V> implements MultiLoadBalancer<T, K, V> {

    private final Map<T, LoadBalancer<K, V>> loadbalancers;
    private final Function<K, V> convertor;
    private final Consumer<V> destructor;
    private final BiPredicate<K, K> matcher;

    public DefaultMultiLoadBalancer(Function<K, V> convertor, Consumer<V> destructor, BiPredicate<K, K> matcher) {
        loadbalancers = new ConcurrentHashMap<>();
        this.convertor = convertor;
        this.destructor = destructor;
        this.matcher = matcher;
    }

    @Override
    public boolean addLoadBalancer(T type, LoadBalancer<K, V> loadBalancer) {
        loadBalancer.setConvertor(this.convertor);
        loadBalancer.setDestructor(this.destructor);
        loadBalancer.setMatcher(this.matcher);
        return loadbalancers.putIfAbsent(type, loadBalancer) == null;
    }

    @Override
    public LoadBalancer<K, V> getLoadBalancer(T type) {
        return loadbalancers.get(type);
    }

    @Override
    public Set<T> allTypes() {
        return loadbalancers.keySet();
    }

    @Override
    public boolean hasType(T type) {
        return loadbalancers.containsKey(type);
    }

    @Override
    public void forEach(Consumer<V> consumer) {
        loadbalancers.values().forEach(l -> l.forEach(consumer));
    }

    @Override
    public void clearLoadBalancer() {
        loadbalancers.clear();
    }

    @Override
    public boolean isEmpty() {
        return loadbalancers.isEmpty();
    }

}
