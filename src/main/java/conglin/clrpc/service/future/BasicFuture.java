package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transport.message.*;

public class BasicFuture extends AbstractFuture {

    private final RequestPayload request;
    private final long messageId;
    private ResponsePayload response;

    public BasicFuture(long messageId, RequestPayload request) {
        super();
        this.request = request;
        this.messageId = messageId;
    }

    @Override
    protected Object doGet() throws RpcServiceException {
        if (response == null)
            return null;
        if (isError()) {
            throw (RpcServiceException) response.result();
        }
        return response.result();
    }

    @Override
    public long identifier() {
        return messageId;
    }

    /**
     * 获得与该 RpcFuture 相关联的 RequestPayload
     * 
     * @return
     */
    public final RequestPayload request() {
        return this.request;
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicResponse
     * 
     * @return
     */
    public final ResponsePayload response() {
        return this.response;
    }

    @Override
    protected void beforeDone(Object result) {
        super.beforeDone(result);
        this.response = (ResponsePayload) result;
        if (response.isError())
            signError();
    }

    @Override
    protected void doRunCallback(Callback callback) {
        if (!isError()) {
            callback.success(response.result());
        } else {
            callback.fail((RpcServiceException) response.result());
        }
    }
}