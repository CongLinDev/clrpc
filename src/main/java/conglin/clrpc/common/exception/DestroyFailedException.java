package conglin.clrpc.common.exception;

public class DestroyFailedException extends Exception {

    private static final long serialVersionUID = -2555711763516733584L;

    public DestroyFailedException() {
        super();
    }

    public DestroyFailedException(String msg) {
        super(msg);
    }
}