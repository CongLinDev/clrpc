package conglin.clrpc.common.util.chain;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * 用于替代 {@link java.util.LinkedList} 的简单单向链表
 */
public interface Chain<T> {

    /**
     * 创建链的一个临时头节点
     * 
     * 可以使用 {@link Chain#trim()} 去除
     * 
     * @param <V>
     * @return
     */
    static <V> Chain<V> newHeader() {
        return new ChainHeader<>();
    }

    /**
     * 创建链的一个临时头节点
     * 
     * 可以使用 {@link Chain#trim()} 去除
     * 
     * @param <V>
     * @param next
     * @return
     */
    static <V> Chain<V> newHeader(Chain<V> next) {
        return new ChainHeader<>(next);
    }

    /**
     * 将节点链接起来
     *
     * @param <V>
     * @param nodes
     * @return
     */
    @SafeVarargs
    static <V> Chain<V> connect(Chain<V>... nodes) {
        if (nodes == null)
            return null;
        Chain<V> header = newHeader();
        Chain<V> next = header;
        for (Chain<V> node : nodes) {
            if (node != null) {
                next.setNext(node);
                next = node;
            }
        }
        return header;
    }

    /**
     * 移除 {@link ChainHeader}
     * 
     * @param <V>
     * @param node
     * @return 第一个非 {@link ChainHeader} 对象
     */
    static <V> Chain<V> trim(Chain<V> node) {
        Chain<V> header = newHeader(node);
        Chain<V> current = header;
        while (current != null) {
            if (current.next() instanceof ChainHeader) {
                current.removeNext();
            } else {
                current = current.next();
            }
        }
        return header.next();
    }

    /**
     * 返回链长度
     * 
     * @param node
     * @return
     */
    static int length(Chain<?> node) {
        int counter = 0;
        while (node != null) {
            counter++;
            node = node.next();
        }
        return counter;
    }

    /**
     * 当期链节点上保存的值
     */
    T value();

    /**
     * 下一个链节点
     * 
     * @return
     */
    Chain<T> next();

    /**
     * 设置为下一个
     * 
     * @param node
     * @return this
     */
    Chain<T> setNext(Chain<T> node);

    /**
     * 返回最后一个非空链节点
     * 
     * @return
     */
    default Chain<T> tail() {
        Chain<T> node = this;
        while (node.next() != null) {
            node = node.next();
        }
        return node;
    }

    /**
     * 将 {@code node} 设为当前链节点的后缀节点
     * 
     * 且不会保留 {@code node} 的后缀节点
     * 
     * @param node this
     * @return
     */
    default Chain<T> addNext(Chain<T> node) {
        return addNext(node, false);
    }

    /**
     * 移除后面的一个节点
     * 
     * @return 被移除的节点
     */
    default Chain<T> removeNext() {
        Chain<T> removedNode = next();
        if (removedNode == null) {
            return null;
        }
        setNext(removedNode.next());
        return removedNode.setNext(null);
    }

    /**
     * 将 {@code node} 设为当前链节点的后缀节点
     * 
     * {@code keepNodeNext} 决定是否保留 {@code node} 的后缀节点
     * 
     * @param node
     * @param keepNodeNext 是否保留新节点的后缀节点
     * @return this
     */
    default Chain<T> addNext(Chain<T> node, boolean keepNodeNext) {
        if (node == null)
            return this;
        (keepNodeNext ? node.tail() : node).setNext(next());
        return setNext(node);
    }

    /**
     * 将 {@code node} 设为当前链节点的最后后缀节点
     * 
     * 且不会保留 {@code node} 的后缀节点
     * 
     * @param node
     * @return this
     */
    default Chain<T> addTail(Chain<T> node) {
        return addTail(node, false);
    }

    /**
     * 将 {@code node} 设为当前链节点的最后后缀节点
     * 
     * {@code keepNodeNext} 决定是否保留 {@code node} 的后缀节点
     * 
     * @param node
     * @param keepNodeNext 是否保留新节点的后缀节点
     * @return this
     */
    default Chain<T> addTail(Chain<T> node, boolean keepNodeNext) {
        if (node == null)
            return this;
        if (!keepNodeNext)
            node.setNext(null);
        return tail().setNext(node);
    }

    /**
     * 从当前链节点开始，对于链上的所有节点的值进行处理
     * 
     * @param consumer
     */
    default void forEach(Consumer<T> consumer) {
        if (consumer == null)
            return;
        Chain<T> node = this;
        while (node != null) {
            consumer.accept(node.value());
            node = node.next();
        }
    }

    /**
     * 从当前链节点开始，寻找所有匹配的值进行处理
     * 
     * @param consumer
     */
    default void matchAndAccept(Predicate<T> predicate, Consumer<T> consumer) {
        if (predicate == null || consumer == null)
            return;
        T value = value();
        if (predicate.test(value))
            consumer.accept(value);

        Chain<T> node = next();
        if (node != null)
            node.matchAndAccept(predicate, consumer);

    }

    /**
     * 从当前链节点开始，寻找第一个匹配的值进行处理
     * 
     * @param predicate
     * @param consumer
     */
    default void firstMatchAndAccept(Predicate<T> predicate, Consumer<T> consumer) {
        if (predicate == null || consumer == null)
            return;
        T value = value();
        if (predicate.test(value)) {
            consumer.accept(value);
        } else {
            Chain<T> node = next();
            if (node != null)
                node.firstMatchAndAccept(predicate, consumer);
        }
    }

    /**
     * 移除 {@link ChainHeader}
     * 
     * @return 第一个非 {@link ChainHeader} 对象
     */
    default Chain<T> trim() {
        return trim(this);
    }

    /**
     * 返回链长度
     * 
     * @return
     */
    default int length() {
        return length(this);
    }
}

final class ChainHeader<T> implements Chain<T> {

    private Chain<T> next;

    public ChainHeader() {
        this(null);
    }

    public ChainHeader(Chain<T> next) {
        this.next = next;
    }

    @Override
    public T value() {
        return null;
    }

    @Override
    public Chain<T> next() {
        return next;
    }

    @Override
    public Chain<T> setNext(Chain<T> node) {
        next = node;
        return this;
    }
}