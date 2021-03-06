package conglin.clrpc.common.exception;

/**
 * 销毁异常
 * 
 * 当调用 {@link conglin.clrpc.common.Destroyable#destroy()} 失败时抛出
 */
public class DestroyFailedException extends Exception {

    private static final long serialVersionUID = -2555711763516733584L;

    public DestroyFailedException() {
        super();
    }

    public DestroyFailedException(String msg) {
        super(msg);
    }
}