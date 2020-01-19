package conglin.clrpc.common.exception;

import conglin.clrpc.common.Callback;

public class FutureRepeatedCallbackException extends RuntimeException {

    private static final long serialVersionUID = 8895227138373147043L;

    private final Callback existed;

    public FutureRepeatedCallbackException(Callback existed) {
        super("Future callback is repeated");
        this.existed = existed;
    }

    /**
     * 返回已经存在的那个 {@link conglin.clrpc.common.Callback}
     * 
     * @return
     */
    public Callback existedCallback() {
        return existed;
    }

}