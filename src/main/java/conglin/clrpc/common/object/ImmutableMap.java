package conglin.clrpc.common.object;

import java.io.Serializable;
import java.util.*;

/**
 * 不可变的map
 */
public class ImmutableMap<K, V> implements Map<K, V>, Serializable {

    static class ImmutableMapBuilder<K, V> {
        private final List<K> keys;
        private final List<V> values;
        public ImmutableMapBuilder(int size) {
            keys = new ArrayList<>(size);
            values = new ArrayList<>(size);
        }

        public ImmutableMapBuilder() {
            this(16);
        }

        public ImmutableMapBuilder<K, V> add(K key, V value) {
            keys.add(key);
            values.add(value);
            return this;
        }

        public ImmutableMap<K, V> build() {
            return new ImmutableMap<>(keys, values);
        }
    }

    @SuppressWarnings("rawtypes")
    private final static ImmutableMap EMPTY_MAP = new ImmutableMap();

    @SuppressWarnings("unchecked")
    public static <K,V> ImmutableMap<K,V> empty() {
        return (ImmutableMap<K,V>) EMPTY_MAP;
    }

    private final ImmutableList<K> keys;

    private final ImmutableList<V> values;

    protected static <T> HashSet<T> distinct(Collection<T> collection) {
        return new HashSet<>(collection);
    }

    private ImmutableMap() {
        this.keys = new ImmutableList<>();
        this.values = new ImmutableList<>();
    }

    public ImmutableMap(K[] keys, V[] values) {
        if(keys == null || values == null || keys.length != values.length) {
            throw new IllegalArgumentException();
        }
        this.keys = new ImmutableList<>(keys);
        this.values = new ImmutableList<>(values);
    }

    public ImmutableMap(Collection<K> keys, Collection<V> values) {
        if(keys == null || values == null || keys.size() != values.size()) {
            throw new IllegalArgumentException();
        }
        this.keys = new ImmutableList<>(keys);
        this.values = new ImmutableList<>(values);
    }

    @Override
    public int size() {
        return keys.size();
    }

    @Override
    public boolean isEmpty() {
        return keys.size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return keys.contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values.contains(value);
    }

    @Override
    public V get(Object key) {
        if(key == null) {
            throw new NullPointerException();
        }
        int index = keys.indexOf(key);
        return index < 0 ? null : values.get(index);
    }

    @Override
    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Set<K> keySet() {
        return new ImmutableSet<>(keys);
    }

    @Override
    public Collection<V> values() {
        return values;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        // TODO: 
        return null;
    }
}
