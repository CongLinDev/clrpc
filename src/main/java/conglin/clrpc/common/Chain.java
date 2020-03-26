package conglin.clrpc.common;

import java.util.function.Consumer;

public interface Chain<T> {

    /**
     * 返回最后一个非空链节点
     * 
     * @param <V>
     * @param node
     * @return
     */
    static <V> Chain<V> tail(Chain<V> node) {
        while (node != null && node.next() != null) {
            node = node.next();
        }
        return node;
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
        for (int i = 0; i < nodes.length - 1; i++) {
            nodes[i].setNext(nodes[i + 1]);
        }
        return nodes[0];
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
     * 返回最后一个非空链节点
     * 
     * @return
     */
    default Chain<T> tail() {
        return tail(this);
    }

    /**
     * 去除该节点后面的所有节点
     * 
     * @return 去除的所有节点的头节点
     */
    default Chain<T> trim() {
        Chain<T> next = next();
        setNext(null);
        return next;
    }

    /**
     * 设置为下一个
     * 
     * @param node
     * @return this
     */
    Chain<T> setNext(Chain<T> node);

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
    Chain<T> removeNext();

    /**
     * 将 {@code node} 设为当前链节点的后缀节点
     * 
     * {@code keepNodeNext} 决定是否保留 {@code node} 的后缀节点
     * 
     * @param node
     * @param keepNodeNext 是否保留新节点的后缀节点
     * @return this
     */
    Chain<T> addNext(Chain<T> node, boolean keepNodeNext);

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
    Chain<T> addTail(Chain<T> node, boolean keepNodeNext);

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
}