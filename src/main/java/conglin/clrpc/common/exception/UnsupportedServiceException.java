package conglin.clrpc.common.exception;

/**
 * RPC服务异常
 * 
 * 未找到服务后抛出
 */
public class UnsupportedServiceException extends RpcServiceException {

    private static final long serialVersionUID = 2977212781905153793L;

    public UnsupportedServiceException(String serviceName) {
        super("UnsupportedService: " + serviceName);
    }
}