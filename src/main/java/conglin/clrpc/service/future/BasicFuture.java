package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.ServiceException;
import conglin.clrpc.service.context.InvocationContext;
import conglin.clrpc.transport.message.*;

public class BasicFuture extends AbstractFuture {

    private final InvocationContext invocationContext;
    private final long messageId;
    private ResponsePayload response;

    public BasicFuture(long messageId, InvocationContext invocationContext) {
        super();
        this.invocationContext = invocationContext;
        this.messageId = messageId;
    }

    @Override
    protected Object doGet() throws ServiceException {
        if (response == null)
            return null;
        if (isError()) {
            throw (ServiceException) response.result();
        }
        return response.result();
    }

    @Override
    public long identifier() {
        return messageId;
    }

    /**
     * 获得与该 InvocationFuture 相关联的 RequestPayload
     * 
     * @return
     */
    public final InvocationContext context() {
        return this.invocationContext;
    }

    /**
     * 获得与该 InvocationFuture 相关联的 BasicResponse
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
            callback.fail((ServiceException) response.result());
        }
    }
}