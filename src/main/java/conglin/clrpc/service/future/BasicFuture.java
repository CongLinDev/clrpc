package conglin.clrpc.service.future;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.exception.ServiceException;

public class BasicFuture extends AbstractFuture {

    private Object result;

    public BasicFuture() {
        super();
    }

    @Override
    protected Object doGet() throws ServiceException {
        if (isError()) {
            throw (ServiceException) result;
        }
        return result;
    }

    @Override
    protected void beforeDone(Object result) {
        super.beforeDone(result);
        this.result = result;
    }

    @Override
    protected void doRunCallback(Callback callback) {
        if (!isError()) {
            callback.success(result);
        } else {
            callback.fail((ServiceException) result);
        }
    }
}