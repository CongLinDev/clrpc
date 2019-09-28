package conglin.clrpc.service.future;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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
     * 将future添加进来
     * @param future
     * @return
     */
    public AbstractCompositeFuture add(RpcFuture future){
        futures.add(future);
        return this;
    }

    // /**
    //  * 将future添加进来
    //  * @param collection
    //  * @return
    //  */
    // public RpcCompositeFuture add(Collection<? extends RpcFuture> collection){
    //     futures.addAll(collection);
    //     return this;
    // }

    /**
     * 返回当前列表下的future数量
     * 若其中也存在 {@link RpcCompositeFuture} 则不会递归计算
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

class FutureCancelledException extends Exception{

    private static final long serialVersionUID = -6235027907224439419L;

    private final RpcFuture future;

    public FutureCancelledException(RpcFuture future){
        this.future = future;
    }

    public RpcFuture getFuture(){
        return future;
    }
}
