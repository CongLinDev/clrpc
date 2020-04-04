package conglin.clrpc.common.util.chain;

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
}