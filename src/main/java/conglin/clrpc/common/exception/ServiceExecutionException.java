package conglin.clrpc.common.exception;

public class ServiceExecutionException extends RpcServiceException {

    private static final long serialVersionUID = 7912835199025899934L;

    public ServiceExecutionException(String desc) {
        super(desc);
    }

    public ServiceExecutionException(String desc, Throwable throwable) {
        super(desc, throwable);
    }

    public ServiceExecutionException(Throwable throwable) {
        super(throwable);
    }
}