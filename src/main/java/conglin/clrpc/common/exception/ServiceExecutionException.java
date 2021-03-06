package conglin.clrpc.common.exception;

/**
 * RPC服务异常
 * 
 * 执行服务错误后抛出
 */
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