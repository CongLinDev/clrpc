package conglin.clrpc.invocation.future;

import java.util.concurrent.CancellationException;

import conglin.clrpc.common.Callback;
import conglin.clrpc.common.ServiceException;

public class BasicFuture extends AbstractFuture {

    private Object result;

    public BasicFuture() {
        super();
    }

    @Override
    protected Object doGet() throws ServiceException {
        if (isCancelled()) {
            throw new CancellationException();
        }
        if (isError()) {
            throw (ServiceException) result;
        }
        return result;
    }

    @Override
    protected void beforeRunCallback(Object result) {
        super.beforeRunCallback(result);
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