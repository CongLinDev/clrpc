package conglin.clrpc.common.object;

import java.util.*;

public class ImmutableList <V> implements List<V> {

    private final Object[] elementData;

    public ImmutableList() {
        elementData = new Object[0];
    }

    public ImmutableList(V[] elements) {
        this(elements, 0, elements.length);
    }

    public ImmutableList(V[] elements, int start, int end) {
        if(start != end) {
            elementData = new Object[end - start];
            System.arraycopy(elements, start, elementData, 0, elements.length);
        } else {
            elementData = new Object[0];
        }
    }

    public ImmutableList(Collection<? extends V> elements) {
        elementData = elements.toArray();
    }

    public ImmutableList(List<? extends V> list, int start, int end) {
        if(start != end) {
            elementData = new Object[end - start];
            for(int i = 0; i < elementData.length; i++) {
                elementData[i] = list.get(i + start);
            }
        } else {
            elementData = new Object[0];
        }
    }

    @Override
    public int size() {
        return elementData.length;
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) >= 0;
    }

    @Override
    public Iterator<V> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        return Arrays.copyOf(elementData, elementData.length);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T[] toArray(T[] a) {
        if (a.length < elementData.length)
            // Make a new array of a's runtime type, but my contents:
            return (T[]) Arrays.copyOf(elementData, elementData.length, a.getClass());
        System.arraycopy(elementData, 0, a, 0, elementData.length);
        if (a.length > elementData.length)
            a[elementData.length] = null;
        return a;
    }

    @Override
    public boolean add(V v) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object ele : c) {
            if(!contains(ele))
                return false;
        }
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(int index, Collection<? extends V> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public V get(int index) {
        return elementData(index);
    }

    @Override
    public V set(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void add(int index, V element) {
        throw new UnsupportedOperationException();
    }

    @Override
    public V remove(int index) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int indexOf(Object o) {
        return indexOfRange(o, 0, elementData.length);
    }

    @Override
    public int lastIndexOf(Object o) {
        return lastIndexOfRange(o, 0, elementData.length);
    }

    @Override
    public ListIterator<V> listIterator() {
        return new ImmutableListIterator();
    }

    @Override
    public ListIterator<V> listIterator(int index) {
        return new ImmutableListIterator(index);
    }

    @Override
    public List<V> subList(int fromIndex, int toIndex) {
        return new ImmutableList<>(this, fromIndex, toIndex);
    }

    protected int indexOfRange(Object o, int start, int end) {
        Object[] es = elementData;
        if (o == null) {
            for (int i = start; i < end; i++) {
                if (es[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = start; i < end; i++) {
                if (o.equals(es[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    protected int lastIndexOfRange(Object o, int start, int end) {
        Object[] es = elementData;
        if (o == null) {
            for (int i = end - 1; i >= start; i--) {
                if (es[i] == null) {
                    return i;
                }
            }
        } else {
            for (int i = end - 1; i >= start; i--) {
                if (o.equals(es[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    @SuppressWarnings("unchecked")
    protected V elementData(int index) {
        return (V)elementData[index];
    }

    class ImmutableListIterator implements ListIterator<V> {

        private int index = 0;

        public ImmutableListIterator(int index) {
            this.index = index;
        }

        public ImmutableListIterator() {
            this(0);
        }

        @Override
        public boolean hasNext() {
            return index != ImmutableList.this.size();
        }

        @Override
        public V next() {
            return ImmutableList.this.get(index++);
        }

        @Override
        public boolean hasPrevious() {
            return index != 0;
        }

        @Override
        public V previous() {
            return ImmutableList.this.get(--index);
        }

        @Override
        public int nextIndex() {
            return index;
        }

        @Override
        public int previousIndex() {
            return index - 1;
        }

        @Override
        public void remove() {
            throw new UnsupportedOperationException();
        }

        @Override
        public void set(V v) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void add(V v) {
            throw new UnsupportedOperationException();
        }
    }
}


