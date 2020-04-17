package conglin.clrpc.common;

/**
 * 带有数据的 {@link Callback}
 */
public interface DataCallback extends Callback {

    /**
     * 返回数据
     * 
     * @return
     */
    Object data();
}