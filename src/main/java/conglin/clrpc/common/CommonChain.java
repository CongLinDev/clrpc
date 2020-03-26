package conglin.clrpc.common;

public class CommonChain<T> implements Chain<T> {

    private T value;

    private Chain<T> nextNode;

    public CommonChain(T value) {
        this(value, null);
    }

    public CommonChain(T value, Chain<T> next) {
        this.value = value;
        this.nextNode = next;
    }

    @Override
    public T value() {
        return value;
    }

    @Override
    public Chain<T> next() {
        return nextNode;
    }

    @Override
    public Chain<T> setNext(Chain<T> node) {
        nextNode = node;
        return this;
    }

    @Override
    public Chain<T> addNext(Chain<T> node, boolean keepNodeNext) {
        if(node != null){
            (keepNodeNext ? Chain.tail(node) : node).setNext(nextNode);
            nextNode = node;
        }
        return this;
    }

    @Override
    public Chain<T> removeNext() {
        if(nextNode == null)
            return null;
        Chain<T> removed = nextNode;
        nextNode = removed.next();
        return removed;
    }

    @Override
    public Chain<T> addTail(Chain<T> node, boolean keepNodeNext) {
        if(node != null) {
            tail().setNext(node);
            if(!keepNodeNext)
                node.setNext(null);
        }
        return this;
    }
}