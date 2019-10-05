package conglin.clrpc.service.future;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import conglin.clrpc.common.exception.FutureCancelledException;

abstract public class AbstractCompositeFuture extends AbstractFuture {

    protected final List<RpcFuture> futures;

    public AbstractCompositeFuture() {
        super();
        this.futures = new LinkedList<>();
    }

    public AbstractCompositeFuture(Collection<? extends RpcFuture> collection){
        super();
        this.futures = new LinkedList<>(collection);
    }

    /**
     * 将future合并进来
     * @param future
     * @return
     */
    public boolean combine(RpcFuture future){
        beforeCombine(future);
        return futures.add(future);
    }

    /**
     * 将future合并进来
     * @param collection
     * @return
     */
    public boolean combine(Collection<? extends RpcFuture> collection){
        collection.forEach(this::beforeCombine);
        return futures.addAll(collection);
    }

    /**
     * 合并前的操作
     * @param future
     */
    abstract protected void beforeCombine(RpcFuture future);

    /**
     * 返回当前列表下的future数量
     * 若其中也存在 {@link AbstractCompositeFuture} 则不会递归计算
     * @return
     */
    public int size(){
        return futures.size();
    }

    @Override
    public void retry() {
        futures.parallelStream()
                .filter(RpcFuture::isPending)
                .forEach(RpcFuture::retry);
    }

    @Override
    public long identifier() {
        if(size() <= 0) return 0;
        return futures.get(0).identifier();
    }


    /**
     * 用于检查当前已经完成的子Future是否完成
     * @return
     * @throws FutureCancelledException 若有{@code RpcFuture} 取消，抛出异常
     */
    protected boolean checkCompleteFuture() throws FutureCancelledException {
        for(RpcFuture f : futures){
            if(f.isPending()) return false;
            else if(f.isCancelled()) 
                throw new FutureCancelledException(f);
        }
        return true;
    }

}


