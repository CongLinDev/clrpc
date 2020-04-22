package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class BasicFuture extends AbstractFuture {

    private final BasicRequest request;
    private BasicResponse response;

    public BasicFuture(BasicRequest request) {
        super();
        this.request = request;
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
        return request.messageId();
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicRequest
     * 
     * @return
     */
    public BasicRequest request() {
        return this.request;
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicResponse
     * 
     * @return
     */
    public BasicResponse response() {
        return this.response;
    }

    @Override
    protected void beforeDone(Object result) {
        this.response = (BasicResponse) result;
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