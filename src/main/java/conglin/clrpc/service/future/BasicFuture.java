package conglin.clrpc.service.future;

import conglin.clrpc.common.exception.RpcServiceException;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class BasicFuture extends AbstractFuture {

    private final BasicRequest request;
    protected BasicResponse response;

    public BasicFuture(BasicRequest request) {
        super();
        this.request = request;
    }

    @Override
    protected Object doGet() throws RpcServiceException {
        if (response == null)
            return null;
        if (response.isError()) {
            setError();
            throw (RpcServiceException) response.getResult();
        }
        return response.getResult();
    }

    @Override
    public long identifier() {
        return request.getMessageId();
    }

    /**
     * 获得与该 RpcFuture 相关联的 BasicRequest
     * 
     * @return
     */
    public BasicRequest request() {
        return this.request;
    }

    @Override
    protected void beforeDone(Object result) {
        this.response = (BasicResponse) result;
    }

    /**
     * 运行回调函数
     * 
     * @param callback
     */
    @Override
    protected void doRunCallback() {
        if (!isError()) {
            this.futureCallback.success(response.getResult());
        } else {
            this.futureCallback.fail((RpcServiceException) response.getResult());
        }
    }
}