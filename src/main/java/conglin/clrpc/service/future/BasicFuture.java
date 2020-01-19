package conglin.clrpc.service.future;

import conglin.clrpc.common.exception.RequestException;
import conglin.clrpc.transport.component.RequestSender;
import conglin.clrpc.transport.message.BasicRequest;
import conglin.clrpc.transport.message.BasicResponse;

public class BasicFuture extends AbstractFuture {

    protected final BasicRequest request;
    protected BasicResponse response;

    protected final RequestSender sender;

    public BasicFuture(RequestSender sender, BasicRequest request) {
        super();
        this.request = request;
        this.sender = sender;
    }

    @Override
    protected Object doGet() throws RequestException {
        if (response == null)
            return null;
        if (response.isError()) {
            setError();
            throw (RequestException) response.getResult();
        }
        return response.getResult();
    }

    @Override
    public void retry() {
        super.retry();
        sender.resendRequest(request);
        resetTime();
    }

    @Override
    public long identifier() {
        return request.getRequestId();
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
            this.futureCallback.fail((RequestException) response.getResult());
        }
    }
}