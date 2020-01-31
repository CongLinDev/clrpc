package conglin.clrpc.service.future;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import conglin.clrpc.common.exception.RpcServiceException;

abstract public class AbstractCompositeFuture extends AbstractFuture {

    private final List<RpcFuture> futures;

    /**
     * 构造一个空的复合Future
     * 
     */
    public AbstractCompositeFuture() {
        this(null);
    }

    /**
     * 构造一个以当前集合为基础的复合Future
     * 
     * @param futures
     */
    public AbstractCompositeFuture(Collection<? extends RpcFuture> futures) {
        super();
        this.futures = (futures == null) ? new LinkedList<>() : new LinkedList<>(futures);
    }

    /**
     * 将future合并进来
     * 
     * @param future
     * @return
     */
    public boolean combine(RpcFuture future) {
        beforeCombine(future);
        return futures.add(future);
    }

    /**
     * 将future合并进来
     * 
     * @param collection
     * @return
     */
    public boolean combine(Collection<? extends RpcFuture> collection) {
        collection.forEach(this::beforeCombine);
        return futures.addAll(collection);
    }

    /**
     * 合并前的操作
     * 
     * @param future
     */
    abstract protected void beforeCombine(RpcFuture future);

    /**
     * 返回当前列表下的future数量 若其中也存在 {@link AbstractCompositeFuture} 则不会递归计算
     * 
     * @return
     */
    public int size() {
        return futures.size();
    }

    @Override
    public void retry() {
        futures.parallelStream().filter(RpcFuture::isPending).forEach(RpcFuture::retry);
    }

    /**
     * 用于检查当前已经完成的子Future是否完成
     * 
     * @return
     * @throws FutureCancelledException 若有{@code RpcFuture} 取消，抛出异常
     */
    protected boolean checkCompleteFuture() throws FutureCancelledException {
        for (RpcFuture f : futures) {
            if (f.isPending())
                return false;
            else if (f.isCancelled())
                throw new FutureCancelledException(f);
        }
        return true;
    }

    @Override
    protected Object doGet() throws RpcServiceException {
        // return results as list
        return futures.stream().map(t -> {
            try {
                return t.get();
            } catch (InterruptedException | ExecutionException | RpcServiceException e) {
                return e;
            }
        }).collect(Collectors.toList());
    }
}
