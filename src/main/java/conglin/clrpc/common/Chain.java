package conglin.clrpc.common;

import java.util.function.Consumer;

public interface Chain<T> {

    /**
     * 返回链上的下一个元素
     * @return
     */
    Chain<T> next();

    /**
     * 设置下一个元素
     * @param next
     * @return
     */
    void next(Chain<T> next);

    /**
     * 直接将下一个元素设为 参数 next
     * @param next
     */
    void setNextDirectly(Chain<T> next);

    /**
     * 能否处理该任务
     * @param task
     * @return
     */
    boolean canHandle(T task);

    /**
     * 处理任务逻辑
     * @param task
     */
    void doHandle(T task);

    /**
     * 处理任务
     * @param task
     */
    default void handle(T task){
        if(canHandle(task)){
            doHandle(task);
        }else{
            next().handle(task);
        }
    }

    /**
     * 从 {@code this} 对象开始到尾部节点，进行批处理
     * @param consumer
     */
    default void forEach(Consumer<Chain<T>> consumer){
        Chain<T> obj = this;
        while(obj != null){
            consumer.accept(obj);
            obj = obj.next();
        }
    }

    /**
     * 链的末尾要进行兜底，防止抛出空指针异常
     * 这里提供了一个简单的链的末尾元素
     */
    Chain<Object> DEFAULT_CHAIN_TAIL = new Chain<Object>() {
        @Override
        public Chain<Object> next() {
            return null;
        }

        @Override
        public void next(Chain<Object> next) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void setNextDirectly(Chain<Object> next) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean canHandle(Object task) {
            return true;
        }

        @Override
        public void doHandle(Object task) {
            throw new UnsupportedOperationException();
        }
    };
}