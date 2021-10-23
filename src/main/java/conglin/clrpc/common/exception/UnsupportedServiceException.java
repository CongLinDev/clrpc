package conglin.clrpc.common.exception;

import java.io.Serial;

/**
 * RPC服务异常
 * 
 * 未找到服务后抛出
 */
public class UnsupportedServiceException extends RpcServiceException {

    @Serial
    private static final long serialVersionUID = 2977212781905153793L;

    public UnsupportedServiceException(String serviceName) {
        super("UnsupportedService: " + serviceName);
    }
}