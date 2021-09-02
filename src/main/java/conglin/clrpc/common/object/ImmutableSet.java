package conglin.clrpc.common.object;

import java.util.*;

public class ImmutableSet<V> implements Set<V> {

    private final ImmutableList<V> elements;

    public ImmutableSet() {
        this.elements = new ImmutableList<>();
    }

    public ImmutableSet(V[] elements) {
        Set<V> set = new HashSet<>();
        Collections.addAll(set, elements);
        this.elements = new ImmutableList<>(set);
    }

    public ImmutableSet(Collection<V> elements) {
        this.elements = new ImmutableList<>(new HashSet<>(elements));
    }

    @Override
    public int size() {
        return elements.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return elements.contains(o);
    }

    @Override
    public Iterator<V> iterator() {
        return elements.iterator();
    }

    @Override
    public Object[] toArray() {
        return elements.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return elements.toArray(a);
    }

    @Override
    public boolean add(V e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return elements.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }
    
}
