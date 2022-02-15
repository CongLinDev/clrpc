package conglin.clrpc.service.future;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

abstract public class AbstractCompositeFuture extends AbstractFuture {

    private final List<InvocationFuture> futures;

    /**
     * 构造一个空的复合Future
     */
    public AbstractCompositeFuture() {
        this(null);
    }

    /**
     * 构造一个以当前集合为基础的复合Future
     * 
     * @param futures
     */
    public AbstractCompositeFuture(Collection<? extends InvocationFuture> futures) {
        super();
        this.futures = (futures == null) ? new ArrayList<>() : new ArrayList<>(futures);
    }

    /**
     * 将future合并进来
     * 
     * @param future
     * @return
     */
    public boolean combine(InvocationFuture future) {
        beforeCombine(future);
        return futures.add(future);
    }

    /**
     * 将future合并进来
     * 
     * @param collection
     * @return
     */
    public boolean combine(Collection<? extends InvocationFuture> collection) {
        collection.forEach(this::beforeCombine);
        return futures.addAll(collection);
    }

    /**
     * 合并前的操作
     * 
     * @param future
     */
    abstract protected void beforeCombine(InvocationFuture future);

    /**
     * 返回当前列表下的future数量 若其中也存在 {@link AbstractCompositeFuture} 则不会递归计算
     * 
     * @return
     */
    public int size() {
        return futures.size();
    }

    /**
     * 用于检查当前已经完成的子Future是否完成
     * 
     * @return
     */
    protected boolean checkCompleteFuture() {
        // 忽略集合中已经取消的 Future 默认为执行完成
        for (InvocationFuture f : futures) {
            if (f.isPending())
                return false;
        }
        return true;
    }

    @Override
    protected List<Object> doGet() {
        // return results as list
        return futures.stream().map(t -> {
            try {
                return t.get();
            } catch (InterruptedException | ExecutionException e) {
                return e;
            }
        }).collect(Collectors.toList());
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException();
    }
}
