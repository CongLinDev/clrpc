package conglin.clrpc.common.exception;

import conglin.clrpc.transfer.message.BasicRequest;

/**
 * 服务执行错误时抛出
 */
public class ServiceExecutionException extends RequestException {

    private static final long serialVersionUID = 4259894135580173323L;

    public ServiceExecutionException(BasicRequest request, Exception exception) {
        super(request, exception.getMessage());  
    }

    @Override
    public String getMessage() {
        return super.getMessage();
    }
}